package pl.su.su_backend.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.event.EventRequestDto;
import pl.su.su_backend.dto.event.EventResponseDto;
import pl.su.su_backend.dto.event.ParticipantResponseDto;
import pl.su.su_backend.dto.event.EventMapper;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.EventParticipantRole;
import pl.su.su_backend.model.enums.EventStatus;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.event.Event;
import pl.su.su_backend.model.event.EventParticipant;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.event.EventRepository;
import pl.su.su_backend.repositories.event.EventParticipantRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static pl.su.su_backend.model.enums.EventStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final EventParticipantRepository participantRepository;
    private final UsersRepository usersRepository;
    private final ActivityLogService activityLogService;
    private final CalendarService calendarService;
    private final PermissionService permissionService;

    public EventResponseDto createEvent(EventRequestDto dto, UUID createdById, String accessToken) {
        log.info("Creating event: {} by user: {}", dto.getTitle(), createdById);
        
        Users creator = usersRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("User not found: " + createdById));

        // Check if user has permission to create events
        if (!permissionService.hasPermission(createdById, PermissionCode.EVENT_CREATE)) {
            throw new RuntimeException("You are not authorized to create events");
        }

        Event event = Event.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .location(dto.getLocation())
                .createdBy(creator)
                .calendarEventId(dto.getCalendarEventId())
                .createdAt(LocalDateTime.now())
                .status(EventStatus.DRAFT) // Created as draft for SU review
                .build();

        Event savedEvent = eventRepository.save(event);

        try {
            String calendarId = calendarService.createCalendarEvent(accessToken, savedEvent);
            if (calendarId != null) {
                savedEvent.setCalendarEventId(calendarId);
                savedEvent = eventRepository.save(savedEvent);
            }
        } catch (Exception ex) {
            log.warn("Graph create failed for event {}: {}", savedEvent.getId(), ex.getMessage());
        }

        addParticipant(savedEvent.getId(), createdById, EventParticipantRole.ORGANIZER, true);
        
        activityLogService.log(createdById, ActionType.EVENT_CREATE, "Created event: " + dto.getTitle());
        
        log.info("Event created successfully with ID: {}", savedEvent.getId());
        return EventMapper.toResponse(savedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEvents(String currentUserEmail) {
        log.info("Fetching events for user: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        // Check if user has permission to view events
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW)) {
            throw new RuntimeException("Access denied: User must have event viewing permission");
        }
        
        List<Event> events;
        
        // Check if user has SU permissions (can see DRAFT and PENDING events)
        if (permissionService.hasPermission(user.getId(), PermissionCode.EVENT_APPROVE)) {
            // SU members can see all events (DRAFT, PENDING, APPROVED)
            events = eventRepository.findAllByOrderByStartDateAsc();
            log.info("SU user {} can see all events including drafts", currentUserEmail);
        } else {
            // Regular users can only see APPROVED events
            events = eventRepository.findByStatusOrderByStartDateAsc(APPROVED);
            log.info("Regular user {} can only see approved events", currentUserEmail);
        }
        
        List<EventResponseDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(EventMapper.toResponse(event));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getAllEventsForAdmin(String currentUserEmail) {
        log.info("Fetching all events for admin: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        // Check if user has permission to view events
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW)) {
            throw new RuntimeException("Access denied: User must have event viewing permission");
        }
        
        // Check if user has admin/SU permissions (can see all events including drafts)
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_APPROVE)) {
            throw new RuntimeException("Access denied: User must have event approval permission to see all events");
        }
        
        List<Event> events = eventRepository.findAllByOrderByStartDateAsc();
        List<EventResponseDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(EventMapper.toResponse(event));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getUpcomingEvents() {
        log.info("Fetching upcoming events");
        List<Event> events = eventRepository.findByStartDateGreaterThanEqualOrderByStartDateAsc(LocalDateTime.now());
        List<EventResponseDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(EventMapper.toResponse(event));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching events in date range: {} to {}", startDate, endDate);
        List<Event> events = eventRepository.findByStartDateBetweenOrderByStartDateAsc(startDate, endDate);
        List<EventResponseDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(EventMapper.toResponse(event));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public EventResponseDto getEventById(UUID eventId, String currentUserEmail) {
        log.info("Fetching event with ID: {} by user: {}", eventId, currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        // Check if user has permission to view events
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW)) {
            throw new RuntimeException("Access denied: User must have event viewing permission");
        }
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
        
        // Check if user can access this specific event based on status
        if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.PENDING) {
            // Only SU members can see DRAFT and PENDING events
            if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_APPROVE)) {
                throw new RuntimeException("Access denied: Only SU members can view draft and pending events");
            }
        }
        
        return EventMapper.toResponse(event);
    }

    public EventResponseDto updateEvent(UUID eventId, EventRequestDto dto, UUID updatedById, String accessToken) {
        log.info("Updating event: {} by user: {}", eventId, updatedById);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        if (!event.getCreatedBy().getId().equals(updatedById) && 
            !permissionService.hasPermission(updatedById, PermissionCode.EVENT_EDIT)) {
            throw new RuntimeException("You are not authorized to edit this event");
        }

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setLocation(dto.getLocation());
        // Keep existing calendarEventId; update on Graph when enabled
        try {
            calendarService.updateCalendarEvent(accessToken, event.getCalendarEventId(), event);
        } catch (Exception ex) {
            log.warn("Graph update failed for event {}: {}", event.getId(), ex.getMessage());
        }

        Event updatedEvent = eventRepository.save(event);
        
        activityLogService.log(updatedById, ActionType.EVENT_EDIT, "Updated event: " + dto.getTitle());
        
        log.info("Event updated successfully with ID: {}", updatedEvent.getId());
        return EventMapper.toResponse(updatedEvent);
    }

    public void deleteEvent(UUID eventId, UUID deletedById, String accessToken) {
        log.info("Deleting event: {} by user: {}", eventId, deletedById);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        if (!event.getCreatedBy().getId().equals(deletedById) &&
            !permissionService.hasPermission(deletedById, PermissionCode.EVENT_DELETE)) {
            throw new RuntimeException("You are not authorized to delete this event");
        }

        // Delete on Graph when enabled
        try {
            calendarService.deleteCalendarEvent(accessToken, event.getCalendarEventId());
        } catch (Exception ex) {
            log.warn("Graph delete failed for event {}: {}", event.getId(), ex.getMessage());
        }

        eventRepository.delete(event);
        
        activityLogService.log(deletedById, ActionType.EVENT_DELETE, "Deleted event: " + event.getTitle());
        
        log.info("Event deleted successfully with ID: {}", eventId);
    }

    public ParticipantResponseDto addParticipant(UUID eventId, UUID userId, EventParticipantRole role, boolean confirmed) {
        log.info("Adding participant {} to event {} with role {}", userId, eventId, role);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));
        
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        if (participantRepository.existsByEvent_IdAndUser_Id(eventId, userId)) {
            throw new RuntimeException("User is already a participant of this event");
        }

        EventParticipant participant = EventParticipant.builder()
                .id(new EventParticipant.Id(eventId, userId))
                .event(event)
                .user(user)
                .role(role)
                .confirmed(confirmed)
                .assignedAt(LocalDateTime.now())
                .build();

        EventParticipant savedParticipant = participantRepository.save(participant);
        
        activityLogService.log(userId, ActionType.EVENT_JOIN, "Joined event: " + event.getTitle());
        
        log.info("Participant added successfully");
        return EventMapper.toResponse(savedParticipant);
    }

    public void removeParticipant(UUID eventId, UUID userId, UUID removedById) {
        log.info("Removing participant {} from event {} by user {}", userId, eventId, removedById);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        // Check if user can remove (creator, has edit permission, or removing themselves)
        if (!event.getCreatedBy().getId().equals(removedById) && 
            !userId.equals(removedById) && 
            !permissionService.hasPermission(removedById, PermissionCode.EVENT_EDIT)) {
            throw new RuntimeException("You are not authorized to remove this participant");
        }

        participantRepository.deleteByEvent_IdAndUser_Id(eventId, userId);
        
        activityLogService.log(userId, ActionType.EVENT_LEAVE, "Left event: " + event.getTitle());
        
        log.info("Participant removed successfully");
    }

    @Transactional(readOnly = true)
    public List<ParticipantResponseDto> getEventParticipants(UUID eventId) {
        log.info("Fetching participants for event: {}", eventId);
        List<EventParticipant> participants = participantRepository.findByEvent_Id(eventId);
        List<ParticipantResponseDto> result = new ArrayList<>();
        for (EventParticipant participant : participants) {
            result.add(EventMapper.toResponse(participant));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getUserEvents(UUID userId) {
        log.info("Fetching events for user: {}", userId);
        List<EventParticipant> participants = participantRepository.findByUser_Id(userId);
        List<EventResponseDto> result = new ArrayList<>();
        for (EventParticipant participant : participants) {
            result.add(EventMapper.toResponse(participant.getEvent()));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getApprovedEvents() {
        log.info("Fetching approved events");
        List<Event> events = eventRepository.findByStatusOrderByStartDateAsc(APPROVED);
        List<EventResponseDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(EventMapper.toResponse(event));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getPendingEvents() {
        log.info("Fetching pending events");
        List<Event> events = eventRepository.findByStatusOrderByCreatedAtDesc(PENDING);
        List<EventResponseDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(EventMapper.toResponse(event));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getDraftEventsForSU(String currentUserEmail) {
        log.info("Fetching draft events for SU: {}", currentUserEmail);
        
        Users user = usersRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUserEmail));
        
        // Check if user has permission to view draft events (SU members)
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW_DRAFTS)) {
            throw new RuntimeException("Access denied: User must have permission to view draft events");
        }
        
        List<Event> events = eventRepository.findByStatusOrderByCreatedAtDesc(DRAFT);
        List<EventResponseDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(EventMapper.toResponse(event));
        }
        return result;
    }

    public EventResponseDto approveEvent(UUID eventId, UUID approvedById) {
        log.info("Approving event {} by user {}", eventId, approvedById);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        event.setStatus(APPROVED);
        Event updatedEvent = eventRepository.save(event);
        
        activityLogService.log(approvedById, ActionType.EVENT_APPROVE, "Approved event: " + event.getTitle());
        
        log.info("Event approved successfully with ID: {}", updatedEvent.getId());
        return EventMapper.toResponse(updatedEvent);
    }

    public EventResponseDto submitEventForApproval(UUID eventId, UUID submittedById) {
        log.info("Submitting event {} for approval by user {}", eventId, submittedById);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        if (!event.getStatus().equals(DRAFT)) {
            throw new RuntimeException("Only DRAFT events can be submitted for approval");
        }

        event.setStatus(PENDING);
        Event updatedEvent = eventRepository.save(event);
        
        activityLogService.log(submittedById, ActionType.EVENT_UPDATE, "Submitted event for approval: " + event.getTitle());
        
        log.info("Event submitted for approval successfully with ID: {}", updatedEvent.getId());
        return EventMapper.toResponse(updatedEvent);
    }

    public EventResponseDto rejectEvent(UUID eventId, UUID rejectedById) {
        log.info("Rejecting event {} by user {}", eventId, rejectedById);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        event.setStatus(REJECTED);
        Event updatedEvent = eventRepository.save(event);
        
        activityLogService.log(rejectedById, ActionType.EVENT_REJECT, "Rejected event: " + event.getTitle());
        
        log.info("Event rejected successfully with ID: {}", updatedEvent.getId());
        return EventMapper.toResponse(updatedEvent);
    }
}
