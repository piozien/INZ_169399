package pl.su.su_backend.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.event.EventMapper;
import pl.su.su_backend.dto.event.EventRequestDto;
import pl.su.su_backend.dto.event.EventResponseDto;
import pl.su.su_backend.dto.event.ParticipantResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.EventParticipantRole;
import pl.su.su_backend.model.enums.EventStatus;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.event.Event;
import pl.su.su_backend.model.event.EventParticipant;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilMemberRepository;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.event.EventParticipantRepository;
import pl.su.su_backend.repositories.event.EventRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.service.user.MailService;
import pl.su.su_backend.service.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final UsersRepository usersRepository;
    private final CouncilRepository councilRepository;
    private final CouncilMemberRepository councilMemberRepository;
    private final UserService userService;
    private final ActivityLogService activityLogService;
    private final PermissionService permissionService;
    private final EventMapper eventMapper;
    private final MailService mailService;

    public EventResponseDto createEvent(EventRequestDto dto, String createdByEmail) {
        Users creator = userService.getUserByEmailEntity(createdByEmail);

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw ApiException.badRequest("Data rozpoczęcia musi być przed datą zakończenia");
        }

        if (dto.getCouncilId() != null) {
            if (!permissionService.hasPermission(creator.getId(), PermissionCode.EVENT_CREATE, dto.getCouncilId())) {
                throw ApiException.forbidden("Brak uprawnień do tworzenia wydarzeń w tym samorządzie.");
            }
        } else {
            if (!permissionService.hasPermission(creator.getId(), PermissionCode.EVENT_CREATE)) {
                throw ApiException.forbidden("Brak uprawnień do tworzenia wydarzeń globalnych.");
            }
        }

        Event event = eventMapper.toEntity(dto);
        event.setCreatedBy(creator);
        event.setStatus(EventStatus.DRAFT);
        event.setCreatedAt(LocalDateTime.now());

        event.setMaxParticipants(dto.getMaxParticipants());
        event.setParticipantsCount(0);

        if (dto.getCouncilId() != null) {
            Council council = councilRepository.findById(dto.getCouncilId())
                    .orElseThrow(() -> ApiException.notFound("Nie znaleziono samorządu"));
            event.setCouncil(council);
        }

        Event savedEvent = eventRepository.save(event);

        addParticipantInternal(savedEvent, creator, EventParticipantRole.ORGANIZER, true, false);

        activityLogService.log(creator.getId(), ActionType.EVENT_CREATE, "Utworzono wydarzenie: " + dto.getTitle());

        return eventMapper.toResponse(savedEvent);
    }

    public EventResponseDto updateEvent(UUID eventId, EventRequestDto dto, UUID updatedById) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono wydarzenia"));

        UUID councilId = event.getCouncil() != null ? event.getCouncil().getId() : null;
        boolean canEdit = permissionService.hasPermission(updatedById, PermissionCode.EVENT_EDIT, councilId);
        boolean isCreator = event.getCreatedBy().getId().equals(updatedById);

        if (!canEdit && !isCreator) {
            throw ApiException.forbidden("Brak uprawnień do edycji tego wydarzenia");
        }

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setLocation(dto.getLocation());

        event.setMaxParticipants(dto.getMaxParticipants());

        return eventMapper.toResponse(eventRepository.save(event));
    }

    public void deleteEvent(UUID eventId, UUID deletedById) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono wydarzenia"));

        UUID councilId = event.getCouncil() != null ? event.getCouncil().getId() : null;
        boolean canDelete = permissionService.hasPermission(deletedById, PermissionCode.EVENT_DELETE, councilId);
        boolean isCreator = event.getCreatedBy().getId().equals(deletedById);

        if (!canDelete && !isCreator) {
            throw ApiException.forbidden("Brak uprawnień do usunięcia");
        }

        eventRepository.delete(event);
        activityLogService.log(deletedById, ActionType.EVENT_DELETE, "Usuwanie wydarzenia: " + event.getTitle());
    }

    public ParticipantResponseDto addParticipant(UUID eventId, UUID userId, EventParticipantRole role, boolean confirmed) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> ApiException.notFound("Nie znaleziono wydarzenia"));
        Users user = usersRepository.findById(userId).orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));

        if (participantRepository.existsByEvent_IdAndUser_Id(eventId, userId)) {
            throw ApiException.conflict("Użytkownik już bierze udział w wydarzeniu");
        }

        checkParticipantsLimit(event, user);

        return addParticipantInternal(event, user, role, confirmed, true);
    }

    private ParticipantResponseDto addParticipantInternal(Event event, Users user, EventParticipantRole role, boolean confirmed, boolean sendEmail) {

        boolean isExempt = isUserExemptFromLimit(user, event.getCouncil());

        EventParticipant participant = EventParticipant.builder()
                .id(new EventParticipant.Id(event.getId(), user.getId()))
                .event(event)
                .user(user)
                .role(role)
                .confirmed(confirmed)
                .assignedAt(LocalDateTime.now())
                .build();

        participantRepository.save(participant);

        if (!isExempt) {
            event.setParticipantsCount(event.getParticipantsCount() + 1);
            eventRepository.save(event);
        }

        if (sendEmail) {
            sendCalendarInvitation(event, user);
        }

        return eventMapper.toParticipantResponse(participant);
    }

    public void removeParticipant(UUID eventId, UUID userId, UUID removedById) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> ApiException.notFound("Nie znaleziono wydarzenia"));
        Users user = usersRepository.findById(userId).orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));
        UUID councilId = event.getCouncil() != null ? event.getCouncil().getId() : null;

        boolean isCreator = event.getCreatedBy().getId().equals(removedById);
        boolean isSelf = userId.equals(removedById);
        boolean hasPerm = permissionService.hasPermission(removedById, PermissionCode.EVENT_REMOVE_PARTICIPANT, councilId);

        if (!isCreator && !isSelf && !hasPerm) {
            throw ApiException.forbidden("Brak uprawnień");
        }

        boolean wasExempt = isUserExemptFromLimit(user, event.getCouncil());

        participantRepository.deleteByEvent_IdAndUser_Id(eventId, userId);

        if (!wasExempt && event.getParticipantsCount() > 0) {
            event.setParticipantsCount(event.getParticipantsCount() - 1);
            eventRepository.save(event);
        }

        activityLogService.log(userId, ActionType.EVENT_LEAVE, "Opuszczono wydarzenie: " + event.getTitle());
    }


    private void checkParticipantsLimit(Event event, Users user) {
        if (event.getMaxParticipants() == null) {
            return;
        }

        if (isUserExemptFromLimit(user, event.getCouncil())) {
            return;
        }

        if (event.getParticipantsCount() >= event.getMaxParticipants()) {
            throw ApiException.conflict("Osiągnięto limit miejsc na to wydarzenie (" + event.getMaxParticipants() + ")");
        }
    }

    private boolean isUserExemptFromLimit(Users user, Council council) {
        boolean hasGlobalExemptRole = user.getUserRoles().stream()
                .anyMatch(ur -> {
                    RoleCode code = ur.getRole().getRoleCode();
                    return code == RoleCode.DYREKTOR ||
                            code == RoleCode.ZASTEPCA_DYREKTORA ||
                            code == RoleCode.ADMINISTRATOR;
                });

        if (hasGlobalExemptRole) {
            return true;
        }

        if (council != null) {
            Optional<CouncilMember> memberOpt = councilMemberRepository.findByCouncilIdAndUserId(council.getId(), user.getId());
            if (memberOpt.isPresent()) {
                RoleCode code = memberOpt.get().getRole();
                return code == RoleCode.OPIEKUN_SU ||
                        code == RoleCode.PRZEWODNICZACY_SU ||
                        code == RoleCode.ZASTEPCA_DYREKTORA;
            }
        }

        return false;
    }


    private void sendCalendarInvitation(Event event, Users user) {
        try {
            mailService.sendEventInvitation(
                    user.getEmail(),
                    user.getFullName(),
                    event
            );
        } catch (Exception ex) {
            log.warn("The invitation email could not be sent: {}", ex.getMessage());
        }
    }


    public EventResponseDto approveEvent(UUID eventId, UUID approvedById) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> ApiException.notFound("Nie znaleziono wydarzenia"));
        UUID councilId = event.getCouncil() != null ? event.getCouncil().getId() : null;

        if (!permissionService.hasPermission(approvedById, PermissionCode.EVENT_APPROVE, councilId)) {
            throw ApiException.forbidden("Brak uprawnień do zatwierdzenia");
        }
        event.setStatus(EventStatus.APPROVED);
        Event updated = eventRepository.save(event);
        activityLogService.log(approvedById, ActionType.EVENT_APPROVE, "Zatwierdzono wydarzenie: " + event.getTitle());
        return eventMapper.toResponse(updated);
    }

    public EventResponseDto rejectEvent(UUID eventId, UUID rejectedById) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> ApiException.notFound("Nie znaleziono wydarzenia"));
        UUID councilId = event.getCouncil() != null ? event.getCouncil().getId() : null;

        if (!permissionService.hasPermission(rejectedById, PermissionCode.EVENT_APPROVE, councilId)) {
            throw ApiException.forbidden("Brak uprawnień do odrzucenia");
        }
        event.setStatus(EventStatus.REJECTED);
        Event updated = eventRepository.save(event);
        activityLogService.log(rejectedById, ActionType.EVENT_REJECT, "Odrzucono wydarzenie: " + event.getTitle());
        return eventMapper.toResponse(updated);
    }

    @Transactional
    public EventResponseDto pendingEvent(UUID eventId, UUID userId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> ApiException.notFound("Nie znaleziono wydarzenia"));
        UUID councilId = event.getCouncil() != null ? event.getCouncil().getId() : null;

        if (!permissionService.hasPermission(userId, PermissionCode.EVENT_EDIT, councilId)) {
            throw ApiException.forbidden("Brak uprawnień do zatwierdzenia");
        }
        event.setStatus(EventStatus.PENDING);
        Event updated = eventRepository.save(event);
        activityLogService.log(userId, ActionType.EVENT_EDIT, "Ustawiono wydarzenie: " + event.getTitle() + " jako oczekujące");
        return eventMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEvents(String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);
        if (permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW_DRAFTS)) {
            return eventRepository.findAllByOrderByStartDateAsc().stream()
                    .map(eventMapper::toResponse).collect(Collectors.toList());
        } else {
            return eventRepository.findByStatusOrderByStartDateAsc(EventStatus.APPROVED).stream()
                    .map(eventMapper::toResponse).collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsByCouncilId(UUID councilId) {
        return eventRepository.findByCouncilIdOrderByStartDateDesc(councilId).stream()
                .map(eventMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getUpcomingEvents() {
        return eventRepository.findByStatusAndEndDateGreaterThanOrderByStartDateAsc(
                        EventStatus.APPROVED, LocalDateTime.now())
                .stream()
                .map(eventMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventResponseDto getEventById(UUID eventId, String currentUserEmail) {
        Users user = userService.getUserByEmailEntity(currentUserEmail);
        Event event = eventRepository.findById(eventId).orElseThrow(() -> ApiException.notFound("Nie znaleziono wydarzenia"));
        UUID councilId = event.getCouncil() != null ? event.getCouncil().getId() : null;

        if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.PENDING) {
            boolean isAuthor = event.getCreatedBy().getId().equals(user.getId());
            boolean canApprove = permissionService.hasPermission(user.getId(), PermissionCode.EVENT_APPROVE, councilId);
            boolean canViewDrafts = permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW_DRAFTS, councilId);

            if (!isAuthor && !canApprove && !canViewDrafts) {
                throw ApiException.forbidden("Brak dostępu do niezatwierdzonego wydarzenia");
            }
        }
        return eventMapper.toResponse(event);
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponseDto> getEventParticipants(UUID eventId) {
        return participantRepository.findByEvent_Id(eventId).stream().map(eventMapper::toParticipantResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getUserEvents(UUID userId) {
        return participantRepository.findByUser_Id(userId).stream().map(p -> eventMapper.toResponse(p.getEvent())).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getPendingEvents() {
        return eventRepository.findByStatusOrderByCreatedAtDesc(EventStatus.PENDING).stream().map(eventMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw ApiException.badRequest("Data rozpoczęcia musi być przed datą zakończenia");
        }
        return eventRepository.findByStartDateBetweenOrderByStartDateAsc(startDate, endDate).stream().map(eventMapper::toResponse).collect(Collectors.toList());
    }
}