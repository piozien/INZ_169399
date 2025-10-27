package pl.su.su_backend.controller.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.event.EventRequestDto;
import pl.su.su_backend.dto.event.EventResponseDto;
import pl.su.su_backend.dto.event.ParticipantResponseDto;
import pl.su.su_backend.model.enums.EventParticipantRole;
import pl.su.su_backend.service.event.EventService;
import pl.su.su_backend.service.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'EVENT_CREATE')")
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventRequestDto dto, @AuthenticationPrincipal User principal,
                                                        @RegisteredOAuth2AuthorizedClient("microsoft") OAuth2AuthorizedClient client) {
        log.info("Creating event: {} by user: {}", dto.getTitle(), principal.getUsername());
        String accessToken = client != null ? client.getAccessToken().getTokenValue() : null;
        UUID userId = userService.getCurrentUserId(principal.getUsername());
        EventResponseDto event = eventService.createEvent(dto, userId, accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'EVENT_VIEW')")
    public ResponseEntity<List<EventResponseDto>> getAllEvents(@AuthenticationPrincipal User principal) {
        log.info("Fetching all approved events for user: {}", principal.getUsername());
        List<EventResponseDto> events = eventService.getAllEvents(principal.getUsername());
        return ResponseEntity.ok(events);
    }


    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponseDto>> getUpcomingEvents() {
        log.info("Fetching upcoming events");
        List<EventResponseDto> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/range")
    @PreAuthorize("hasPermission(null, 'EVENT_VIEW')")
    public ResponseEntity<List<EventResponseDto>> getEventsInDateRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                           LocalDateTime startDate,
                                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                                       LocalDateTime endDate) {
        log.info("Fetching events in date range: {} to {}", startDate, endDate);
        List<EventResponseDto> events = eventService.getEventsInDateRange(startDate, endDate);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("hasPermission(null, 'EVENT_VIEW')")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable UUID eventId, @AuthenticationPrincipal User principal) {
        log.info("Fetching event with ID: {} by user: {}", eventId, principal.getUsername());
        EventResponseDto event = eventService.getEventById(eventId, principal.getUsername());
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasPermission(null, 'EVENT_EDIT')")
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable UUID eventId, @Valid @RequestBody EventRequestDto dto,
                                                        @AuthenticationPrincipal User principal,
                                                        @RegisteredOAuth2AuthorizedClient("microsoft") OAuth2AuthorizedClient client) {
        log.info("Updating event: {} by user: {}", eventId, principal.getUsername());
        UUID updatedById = userService.getCurrentUserId(principal.getUsername());
        String accessToken = client != null ? client.getAccessToken().getTokenValue() : null;
        EventResponseDto event = eventService.updateEvent(eventId, dto, updatedById, accessToken);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasPermission(null, 'EVENT_DELETE')")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId, @AuthenticationPrincipal User principal,
                                            @RegisteredOAuth2AuthorizedClient("microsoft") OAuth2AuthorizedClient client) {
        log.info("Deleting event: {} by user: {}", eventId, principal.getUsername());
        UUID deletedById = userService.getCurrentUserId(principal.getUsername());
        String accessToken = client != null ? client.getAccessToken().getTokenValue() : null;
        eventService.deleteEvent(eventId, deletedById, accessToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/participants/{userId}")
    public ResponseEntity<ParticipantResponseDto> addParticipant(@PathVariable UUID eventId, @PathVariable UUID userId,
                                                                 @RequestParam EventParticipantRole role,
                                                                 @RequestParam(defaultValue = "false") boolean confirmed) {
        log.info("Adding participant {} to event {} with role {}", userId, eventId, role);
        ParticipantResponseDto participant = eventService.addParticipant(eventId, userId, role, confirmed);
        return ResponseEntity.ok(participant);
    }

    @DeleteMapping("/{eventId}/participants/{userId}")
    @PreAuthorize("hasPermission(null, 'EVENT_EDIT')")
    public ResponseEntity<Void> removeParticipant(@PathVariable UUID eventId, @PathVariable UUID userId,
                                                  @AuthenticationPrincipal User principal) {
        log.info("Removing participant {} from event {} by user {}", userId, eventId, principal.getUsername());
        UUID removedById = userService.getCurrentUserId(principal.getUsername());
        eventService.removeParticipant(eventId, userId, removedById);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/participants")
    @PreAuthorize("hasPermission(null, 'EVENT_VIEW')")
    public ResponseEntity<List<ParticipantResponseDto>> getEventParticipants(@PathVariable UUID eventId) {
        log.info("Fetching participants for event: {}", eventId);
        List<ParticipantResponseDto> participants = eventService.getEventParticipants(eventId);
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasPermission(null, 'EVENT_VIEW')")
    public ResponseEntity<List<EventResponseDto>> getUserEvents(@PathVariable UUID userId) {
        log.info("Fetching events for user: {}", userId);
        List<EventResponseDto> events = eventService.getUserEvents(userId);
        return ResponseEntity.ok(events);
    }


    @GetMapping("/pending")
    @PreAuthorize("hasPermission(null, 'EVENT_APPROVE')")
    public ResponseEntity<List<EventResponseDto>> getPendingEvents() {
        log.info("Fetching pending events");
        List<EventResponseDto> events = eventService.getPendingEvents();
        return ResponseEntity.ok(events);
    }

    @PutMapping("/{eventId}/approve")
    @PreAuthorize("hasPermission(null, 'EVENT_APPROVE')")
    public ResponseEntity<EventResponseDto> approveEvent(@PathVariable UUID eventId, @AuthenticationPrincipal User principal) {
        log.info("Approving event {} by user {}", eventId, principal.getUsername());
        UUID approvedById = userService.getCurrentUserId(principal.getUsername());
        EventResponseDto event = eventService.approveEvent(eventId, approvedById);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}/reject")
    @PreAuthorize("hasPermission(null, 'EVENT_APPROVE')")
    public ResponseEntity<EventResponseDto> rejectEvent(@PathVariable UUID eventId, @AuthenticationPrincipal User principal) {
        log.info("Rejecting event {} by user {}", eventId, principal.getUsername());
        UUID rejectedById = userService.getCurrentUserId(principal.getUsername());
        EventResponseDto event = eventService.rejectEvent(eventId, rejectedById);
        return ResponseEntity.ok(event);
    }
}
