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
@CrossOrigin(origins = "*")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> createEvent(@Valid @RequestBody EventRequestDto dto,
                                                       @AuthenticationPrincipal User principal,
                                                       @RegisteredOAuth2AuthorizedClient("microsoft") OAuth2AuthorizedClient client) {
        log.info("Creating event: {} by user: {}", dto.getTitle(), principal.getUsername());
        try {
            String accessToken = client != null ? client.getAccessToken().getTokenValue() : null;
            UUID userId = userService.getCurrentUserId(principal.getUsername());
            EventResponseDto event = eventService.createEvent(dto, userId, accessToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(event);
        } catch (Exception e) {
            log.error("Failed to create event: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getAllEvents(@AuthenticationPrincipal User principal) {
        log.info("Fetching all approved events for user: {}", principal.getUsername());
        try {
            List<EventResponseDto> events = eventService.getAllEvents(principal.getUsername());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch events: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/admin/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getAllEventsForAdmin(@AuthenticationPrincipal User principal) {
        log.info("Fetching all events for admin: {}", principal.getUsername());
        try {
            List<EventResponseDto> events = eventService.getAllEventsForAdmin(principal.getUsername());
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch all events for admin: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/upcoming")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getUpcomingEvents() {
        log.info("Fetching upcoming events");
        try {
            List<EventResponseDto> events = eventService.getUpcomingEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch upcoming events: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getEventsInDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching events in date range: {} to {}", startDate, endDate);
        try {
            List<EventResponseDto> events = eventService.getEventsInDateRange(startDate, endDate);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch events in date range: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable UUID eventId,
                                                        @AuthenticationPrincipal User principal) {
        log.info("Fetching event with ID: {} by user: {}", eventId, principal.getUsername());
        try {
            EventResponseDto event = eventService.getEventById(eventId, principal.getUsername());
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            log.error("Failed to fetch event with ID: {}, error: {}", eventId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable UUID eventId,
                                                       @Valid @RequestBody EventRequestDto dto,
                                                       @AuthenticationPrincipal User principal,
                                                       @RegisteredOAuth2AuthorizedClient("microsoft") OAuth2AuthorizedClient client) {
        log.info("Updating event: {} by user: {}", eventId, principal.getUsername());
        try {
            UUID updatedById = userService.getCurrentUserId(principal.getUsername());
            String accessToken = client != null ? client.getAccessToken().getTokenValue() : null;
            EventResponseDto event = eventService.updateEvent(eventId, dto, updatedById, accessToken);
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            log.error("Failed to update event: {}, error: {}", eventId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId,
                                           @AuthenticationPrincipal User principal,
                                           @RegisteredOAuth2AuthorizedClient("microsoft") OAuth2AuthorizedClient client) {
        log.info("Deleting event: {} by user: {}", eventId, principal.getUsername());
        try {
            UUID deletedById = userService.getCurrentUserId(principal.getUsername());
            String accessToken = client != null ? client.getAccessToken().getTokenValue() : null;
            eventService.deleteEvent(eventId, deletedById, accessToken);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete event: {}, error: {}", eventId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{eventId}/participants/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ParticipantResponseDto> addParticipant(@PathVariable UUID eventId,
                                                                 @PathVariable UUID userId,
                                                                 @RequestParam EventParticipantRole role,
                                                                 @RequestParam(defaultValue = "false") boolean confirmed) {
        log.info("Adding participant {} to event {} with role {}", userId, eventId, role);
        try {
            ParticipantResponseDto participant = eventService.addParticipant(eventId, userId, role, confirmed);
            return ResponseEntity.ok(participant);
        } catch (Exception e) {
            log.error("Failed to add participant: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{eventId}/participants/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeParticipant(@PathVariable UUID eventId,
                                                  @PathVariable UUID userId,
                                                  @AuthenticationPrincipal User principal) {
        log.info("Removing participant {} from event {} by user {}", userId, eventId, principal.getUsername());
        try {
            UUID removedById = userService.getCurrentUserId(principal.getUsername());
            eventService.removeParticipant(eventId, userId, removedById);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to remove participant: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{eventId}/participants")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ParticipantResponseDto>> getEventParticipants(@PathVariable UUID eventId) {
        log.info("Fetching participants for event: {}", eventId);
        try {
            List<ParticipantResponseDto> participants = eventService.getEventParticipants(eventId);
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            log.error("Failed to fetch participants for event: {}, error: {}", eventId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getUserEvents(@PathVariable UUID userId) {
        log.info("Fetching events for user: {}", userId);
        try {
            List<EventResponseDto> events = eventService.getUserEvents(userId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch events for user: {}, error: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/approved")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getApprovedEvents() {
        log.info("Fetching approved events");
        try {
            List<EventResponseDto> events = eventService.getApprovedEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch approved events: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EventResponseDto>> getPendingEvents() {
        log.info("Fetching pending events");
        try {
            List<EventResponseDto> events = eventService.getPendingEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Failed to fetch pending events: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{eventId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> approveEvent(@PathVariable UUID eventId,
                                                       @AuthenticationPrincipal User principal) {
        log.info("Approving event {} by user {}", eventId, principal.getUsername());
        try {
            UUID approvedById = userService.getCurrentUserId(principal.getUsername());
            EventResponseDto event = eventService.approveEvent(eventId, approvedById);
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            log.error("Failed to approve event {}: {}", eventId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{eventId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EventResponseDto> rejectEvent(@PathVariable UUID eventId,
                                                      @AuthenticationPrincipal User principal) {
        log.info("Rejecting event {} by user {}", eventId, principal.getUsername());
        try {
            UUID rejectedById = userService.getCurrentUserId(principal.getUsername());
            EventResponseDto event = eventService.rejectEvent(eventId, rejectedById);
            return ResponseEntity.ok(event);
        } catch (Exception e) {
            log.error("Failed to reject event {}: {}", eventId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
