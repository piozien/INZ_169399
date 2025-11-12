// https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-client-creds-grant-flow 24.10 - 25.10 - 13:30
// https://learn.microsoft.com/en-us/graph/api/resources/event?view=graph-rest-1.0 24.10 - 25.10 - 13:30
package pl.su.su_backend.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
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
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private Environment environment;
    
    @Autowired
    private WebClient webClient;

    public EventResponseDto createEvent(EventRequestDto dto, UUID createdById, String accessToken) {
        log.info("Creating event: {} by user: {}", dto.getTitle(), createdById);
        
        Users creator = usersRepository.findById(createdById)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));

        if (!permissionService.hasPermission(createdById, PermissionCode.EVENT_CREATE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Start date must be before end date");
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
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        List<Event> events;
        
        if (permissionService.hasPermission(user.getId(), PermissionCode.EVENT_APPROVE)) {
            events = eventRepository.findAllByOrderByStartDateAsc();
            log.info("SU user {} can see all events including drafts", currentUserEmail);
        } else {
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
    public List<EventResponseDto> getUpcomingEvents() {
        log.info("Fetching upcoming events");
        List<Event> events = eventRepository.findByStatusAndEndDateGreaterThanOrderByStartDateAsc(
                EventStatus.APPROVED, LocalDateTime.now());
        List<EventResponseDto> result = new ArrayList<>();
        for (Event event : events) {
            result.add(EventMapper.toResponse(event));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<EventResponseDto> getEventsInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching events in date range: {} to {}", startDate, endDate);
        
        if (startDate.isAfter(endDate)) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Start date must be before end date");
        }
        
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
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));

        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Event not found"));

        if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.PENDING) {
            // Only SU members can see DRAFT and PENDING events
            if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_APPROVE)) {
                throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
            }
        }
        
        return EventMapper.toResponse(event);
    }

    public EventResponseDto updateEvent(UUID eventId, EventRequestDto dto, UUID updatedById, String accessToken) {
        log.info("Updating event: {} by user: {}", eventId, updatedById);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Event not found"));

        if (!event.getCreatedBy().getId().equals(updatedById) && 
            !permissionService.hasPermission(updatedById, PermissionCode.EVENT_EDIT)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Start date must be before end date");
        }

        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setStartDate(dto.getStartDate());
        event.setEndDate(dto.getEndDate());
        event.setLocation(dto.getLocation());
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
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Event not found"));

        if (!event.getCreatedBy().getId().equals(deletedById) &&
            !permissionService.hasPermission(deletedById, PermissionCode.EVENT_DELETE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
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
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Event not found"));
        
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));

        if (participantRepository.existsByEvent_IdAndUser_Id(eventId, userId)) {
            throw ApiException.conflict(ErrorCode.VALIDATION_ERROR, "Participant already exists");
        }
        
        if (event.getStatus() != EventStatus.APPROVED) {
            if (!permissionService.hasPermission(userId, PermissionCode.EVENT_EDIT)) {
                throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Only approved events can be joined");
            }
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
        
        // Send Microsoft Calendar invitation if user has Microsoft account
        if (user.getAuthProvider() != null && user.getAuthProvider().name().equals("MICROSOFT")) {
            try {
                sendCalendarInvitation(event, user);
            } catch (Exception ex) {
                log.warn("Failed to send calendar invitation to user {}: {}", user.getEmail(), ex.getMessage());
            }
        }
        
        activityLogService.log(userId, ActionType.EVENT_JOIN, "Joined event: " + event.getTitle());
        
        log.info("Participant added successfully");
        return EventMapper.toResponse(savedParticipant);
    }
    
    private void sendCalendarInvitation(Event event, Users user) {
        log.info("Sending calendar invitation to user {} for event {}", user.getEmail(), event.getTitle());
        
        // Check if event has calendar event ID
        if (event.getCalendarEventId() == null) {
            log.warn("Event {} has no calendar event ID, cannot send invitation", event.getId());
            return;
        }
        
        try {
            String systemToken = getSystemMicrosoftToken();
            
            if (systemToken != null) {
                calendarService.addAttendeeToEvent(systemToken, event.getCalendarEventId(), user.getEmail());
                log.info("Calendar invitation sent to {}", user.getEmail());
            }
        } catch (Exception ex) {
            log.error("Failed to send calendar invitation to {}: {}", user.getEmail(), ex.getMessage());
        }
    }
    
    private String getSystemMicrosoftToken() {
        try {
            // Client Credentials Flow for system-level access
            String clientId = environment.getProperty("app.microsoft.client-id");
            String clientSecret = environment.getProperty("app.microsoft.client-secret");
            String tenantId = environment.getProperty("app.microsoft.tenant-id");
            String scope = environment.getProperty("app.microsoft.scope");
            
            if (clientId == null || clientSecret == null || tenantId == null) {
                log.warn("Microsoft Graph credentials not configured");
                return null;
            }
            
            String tokenUrl = "https://login.microsoftonline.com/" + tenantId + "/oauth2/v2.0/token";
            
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);
            requestBody.add("scope", scope);
            requestBody.add("grant_type", "client_credentials");
            
            Map<String, Object> response = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(requestBody))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            if (response != null && response.containsKey("access_token")) {
                return (String) response.get("access_token");
            }
            
            log.error("Failed to get Microsoft Graph token");
            return null;
            
        } catch (Exception ex) {
            log.error("Error getting Microsoft Graph token: {}", ex.getMessage());
            return null;
        }
    }

    public void removeParticipant(UUID eventId, UUID userId, UUID removedById) {
        log.info("Removing participant {} from event {} by user {}", userId, eventId, removedById);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Event not found"));

        if (!event.getCreatedBy().getId().equals(removedById) && 
            !userId.equals(removedById) && 
            !permissionService.hasPermission(removedById, PermissionCode.EVENT_EDIT)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
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
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "User not found"));
        if (!permissionService.hasPermission(user.getId(), PermissionCode.EVENT_VIEW_DRAFTS)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
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
        
        if (!permissionService.hasPermission(approvedById, PermissionCode.EVENT_APPROVE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Event not found"));

        event.setStatus(APPROVED);
        Event updatedEvent = eventRepository.save(event);
        
        activityLogService.log(approvedById, ActionType.EVENT_APPROVE, "Approved event: " + event.getTitle());
        
        log.info("Event approved successfully with ID: {}", updatedEvent.getId());
        return EventMapper.toResponse(updatedEvent);
    }

    public EventResponseDto submitEventForApproval(UUID eventId, UUID submittedById) {
        log.info("Submitting event {} for approval by user {}", eventId, submittedById);
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Event not found"));

        if (!event.getStatus().equals(DRAFT)) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Only DRAFT events can be submitted");
        }

        event.setStatus(PENDING);
        Event updatedEvent = eventRepository.save(event);
        
        activityLogService.log(submittedById, ActionType.EVENT_UPDATE, "Submitted event for approval: " + event.getTitle());
        
        log.info("Event submitted for approval successfully with ID: {}", updatedEvent.getId());
        return EventMapper.toResponse(updatedEvent);
    }

    public EventResponseDto rejectEvent(UUID eventId, UUID rejectedById) {
        log.info("Rejecting event {} by user {}", eventId, rejectedById);
        
        if (!permissionService.hasPermission(rejectedById, PermissionCode.EVENT_APPROVE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Access denied");
        }
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Event not found"));

        event.setStatus(REJECTED);
        Event updatedEvent = eventRepository.save(event);
        
        activityLogService.log(rejectedById, ActionType.EVENT_REJECT, "Rejected event: " + event.getTitle());
        
        log.info("Event rejected successfully with ID: {}", updatedEvent.getId());
        return EventMapper.toResponse(updatedEvent);
    }
}
