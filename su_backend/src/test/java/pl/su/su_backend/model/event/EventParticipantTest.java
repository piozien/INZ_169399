package pl.su.su_backend.model.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.enums.EventParticipantRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.testsupport.Fixtures;

import java.time.LocalDateTime;
import java.util.UUID;

public class EventParticipantTest {

    @Test
    void onAssignSetsAssignedAtWhenNull() {
        Event event = Event.builder()
                .title("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .build();
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .user(user)
                .role(EventParticipantRole.PARTICIPANT)
                .build();

        Assertions.assertNull(participant.getAssignedAt());
        participant.onAssign();
        Assertions.assertNotNull(participant.getAssignedAt());
    }

    @Test
    void onAssignDoesNotOverrideExistingAssignedAt() {
        LocalDateTime fixed = LocalDateTime.of(2025, 10, 17, 10, 0);
        Event event = Event.builder()
                .title("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .build();
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .user(user)
                .role(EventParticipantRole.PARTICIPANT)
                .assignedAt(fixed)
                .build();

        participant.onAssign();
        Assertions.assertEquals(fixed, participant.getAssignedAt());
    }

    @Test
    void builderSetsAllFields() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Event event = Event.builder()
                .id(eventId)
                .title("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .build();
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        user.setId(userId);
        LocalDateTime now = LocalDateTime.now();

        EventParticipant.Id id = new EventParticipant.Id(eventId, userId);
        EventParticipant participant = EventParticipant.builder()
                .id(id)
                .event(event)
                .user(user)
                .role(EventParticipantRole.ORGANIZER)
                .confirmed(true)
                .assignedAt(now)
                .build();

        Assertions.assertEquals(id, participant.getId());
        Assertions.assertEquals(event, participant.getEvent());
        Assertions.assertEquals(user, participant.getUser());
        Assertions.assertEquals(EventParticipantRole.ORGANIZER, participant.getRole());
        Assertions.assertTrue(participant.getConfirmed());
        Assertions.assertEquals(now, participant.getAssignedAt());
    }

    @Test
    void builderSetsDefaultConfirmedToFalse() {
        Event event = Event.builder()
                .title("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .build();
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .user(user)
                .role(EventParticipantRole.PARTICIPANT)
                .build();

        Assertions.assertFalse(participant.getConfirmed());
    }

    @Test
    void canChangeFieldsViaSetters() {
        Event event = Event.builder()
                .title("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .build();
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        LocalDateTime now = LocalDateTime.now();

        EventParticipant participant = new EventParticipant();
        participant.setEvent(event);
        participant.setUser(user);
        participant.setRole(EventParticipantRole.PARTICIPANT);
        participant.setConfirmed(true);
        participant.setAssignedAt(now);

        Assertions.assertEquals(event, participant.getEvent());
        Assertions.assertEquals(user, participant.getUser());
        Assertions.assertEquals(EventParticipantRole.PARTICIPANT, participant.getRole());
        Assertions.assertTrue(participant.getConfirmed());
        Assertions.assertEquals(now, participant.getAssignedAt());
    }

    @Test
    void hasCorrectDefaultValues() {
        EventParticipant participant = new EventParticipant();
        participant.onAssign();

        Assertions.assertNull(participant.getId());
        Assertions.assertNull(participant.getEvent());
        Assertions.assertNull(participant.getUser());
        Assertions.assertNull(participant.getRole());
        Assertions.assertFalse(participant.getConfirmed());
        Assertions.assertNotNull(participant.getAssignedAt());
    }

    @Test
    void canCreateParticipantWithDifferentRoles() {
        Event event = Event.builder()
                .title("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .build();
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .user(user)
                .role(EventParticipantRole.PARTICIPANT)
                .build();

        Assertions.assertEquals(EventParticipantRole.PARTICIPANT, participant.getRole());

        EventParticipant organizer = EventParticipant.builder()
                .event(event)
                .user(user)
                .role(EventParticipantRole.ORGANIZER)
                .build();

        Assertions.assertEquals(EventParticipantRole.ORGANIZER, organizer.getRole());

    }

    @Test
    void canSetConfirmedToTrue() {
        Event event = Event.builder()
                .title("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .build();
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .user(user)
                .role(EventParticipantRole.PARTICIPANT)
                .confirmed(true)
                .build();

        Assertions.assertTrue(participant.getConfirmed());
    }

    @Test
    void canSetConfirmedToFalse() {
        Event event = Event.builder()
                .title("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .build();
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .user(user)
                .role(EventParticipantRole.PARTICIPANT)
                .confirmed(false)
                .build();

        Assertions.assertFalse(participant.getConfirmed());
    }

    @Test
    void canCreateParticipantWithEmbeddedId() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Event event = Event.builder()
                .id(eventId)
                .title("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .build();
        Users user = Fixtures.simpleUser("Test User", "user@example.com");
        user.setId(userId);

        EventParticipant.Id id = new EventParticipant.Id(eventId, userId);
        EventParticipant participant = EventParticipant.builder()
                .id(id)
                .event(event)
                .user(user)
                .role(EventParticipantRole.PARTICIPANT)
                .build();

        Assertions.assertEquals(eventId, participant.getId().getEventId());
        Assertions.assertEquals(userId, participant.getId().getUserId());
    }

    @Test
    void canChangeConfirmedStatus() {
        Event event = Event.builder()
                .title("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .build();
        Users user = Fixtures.simpleUser("Test User", "user@example.com");

        EventParticipant participant = EventParticipant.builder()
                .event(event)
                .user(user)
                .role(EventParticipantRole.PARTICIPANT)
                .confirmed(false)
                .build();

        Assertions.assertFalse(participant.getConfirmed());

        participant.setConfirmed(true);
        Assertions.assertTrue(participant.getConfirmed());

        participant.setConfirmed(false);
        Assertions.assertFalse(participant.getConfirmed());
    }
}
