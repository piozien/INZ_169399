package pl.su.su_backend.controller.event;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.event.EventRequestDto;
import pl.su.su_backend.dto.event.EventResponseDto;
import pl.su.su_backend.dto.event.ParticipantResponseDto;
import pl.su.su_backend.model.enums.EventParticipantRole;
import pl.su.su_backend.service.auth.AuthenticationService;
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
    private final AuthenticationService authenticationService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventRequestDto dto, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Creating event: {} by user: {}", dto.getTitle(), email);
        EventResponseDto event = eventService.createEvent(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getAllEvents(@AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching all events (including drafts) for user: {}", email);
        List<EventResponseDto> events = eventService.getAllEvents(email);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponseDto>> getUpcomingEvents() {
        log.info("Fetching upcoming events");
        List<EventResponseDto> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/council/{councilId}")
    @PreAuthorize("hasPermission(#councilId, 'Council', 'EVENT_VIEW_DRAFTS') or hasPermission(#councilId, 'Council', 'EVENT_VIEW')")
    public ResponseEntity<List<EventResponseDto>> getCouncilEvents(@PathVariable UUID councilId) {

        return ResponseEntity.ok(eventService.getEventsByCouncilId(councilId));
    }

    @GetMapping("/range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getEventsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching events in date range: {} to {}", startDate, endDate);
        List<EventResponseDto> events = eventService.getEventsInDateRange(startDate, endDate);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable UUID eventId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching event with ID: {} by user: {}", eventId, email);
        EventResponseDto event = eventService.getEventById(eventId, email);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable UUID eventId, @Valid @RequestBody EventRequestDto dto,
                                                        @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Updating event: {} by user: {}", eventId, email);
        UUID updatedById = userService.getCurrentUserId(email);
        EventResponseDto event = eventService.updateEvent(eventId, dto, updatedById);
        return ResponseEntity.ok(event);
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Deleting event: {} by user: {}", eventId, email);
        UUID deletedById = userService.getCurrentUserId(email);
        eventService.deleteEvent(eventId, deletedById);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/participants/join")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParticipantResponseDto> addParticipant(@PathVariable UUID eventId,
                                                                 @RequestParam EventParticipantRole role,
                                                                 @RequestParam(defaultValue = "false") boolean confirmed,
                                                                 @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        UUID userId = userService.getCurrentUserId(email);
        log.info("Adding participant {} to event {} with role {}", userId, eventId, role);
        ParticipantResponseDto participant = eventService.addParticipant(eventId, userId, role, confirmed);
        return ResponseEntity.ok(participant);
    }

    @DeleteMapping("/{eventId}/participants/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeParticipant(@PathVariable UUID eventId, @PathVariable UUID userId,
                                                  @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Removing participant {} from event {} by user {}", userId, eventId, email);
        UUID removedById = userService.getCurrentUserId(email);
        eventService.removeParticipant(eventId, userId, removedById);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/participants")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ParticipantResponseDto>> getEventParticipants(@PathVariable UUID eventId) {
        log.info("Fetching participants for event: {}", eventId);
        List<ParticipantResponseDto> participants = eventService.getEventParticipants(eventId);
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> approveEvent(@PathVariable UUID eventId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Approving event {} by user {}", eventId, email);
        UUID approvedById = userService.getCurrentUserId(email);
        EventResponseDto event = eventService.approveEvent(eventId, approvedById);
        return ResponseEntity.ok(event);
    }

    @PutMapping("/{eventId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> rejectEvent(@PathVariable UUID eventId, @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Rejecting event {} by user {}", eventId, email);
        UUID rejectedById = userService.getCurrentUserId(email);
        EventResponseDto event = eventService.rejectEvent(eventId, rejectedById);
        return ResponseEntity.ok(event);
    }
}