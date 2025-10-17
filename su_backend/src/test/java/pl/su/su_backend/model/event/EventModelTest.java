package pl.su.su_backend.model.event;

import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;

class EventModelTest {

    @Test
    void defaultStatusIsDraftAndIsApprovedFalse() {
        Event event = Event.builder()
                .title("Test")
                .description("Desc")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .build();

        // @PrePersist
        event.onCreate();

        Assertions.assertEquals(EventStatus.DRAFT, event.getStatus());
        Assertions.assertFalse(event.isApproved());
    }

    @Test
    void isApprovedTrueWhenStatusApproved() {
        Event event = Event.builder()
                .title("A")
                .description("B")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(1))
                .status(EventStatus.APPROVED)
                .build();

        Assertions.assertTrue(event.isApproved());
    }

    @Test
    void onCreateSetsCreatedAtWhenNull() {
        Event event = Event.builder()
                .title("C")
                .description("D")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(3))
                .build();

        Assertions.assertNull(event.getCreatedAt());
        event.onCreate();
        Assertions.assertNotNull(event.getCreatedAt());
    }

    @Test
    void onCreateDoesNotOverrideExistingCreatedAt() {
        LocalDateTime fixed = LocalDateTime.of(2025, 1, 1, 12, 0);
        Event event = Event.builder()
                .title("E")
                .description("F")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(2))
                .createdAt(fixed)
                .build();

        event.onCreate();
        Assertions.assertEquals(fixed, event.getCreatedAt());
    }

    @Test
    void participantsCollectionIsInitializedAndEmptyByDefault() {
        Event event = Event.builder()
                .title("G")
                .description("H")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(1))
                .build();

        Assertions.assertNotNull(event.getParticipants());
        Assertions.assertTrue(event.getParticipants().isEmpty());
    }

    @Test
    void statusRemainsUnchangedIfProvidedBeforePersist() {
        Event event = Event.builder()
                .title("I")
                .description("J")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(1))
                .status(EventStatus.CANCELLED)
                .build();

        event.onCreate();
        Assertions.assertEquals(EventStatus.CANCELLED, event.getStatus());
    }

    @Test
    void calendarEventIdIsOptional() {
        Event event = Event.builder()
                .title("K")
                .description("L")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusHours(1))
                .build();

        Assertions.assertNull(event.getCalendarEventId());

        String externalId = UUID.randomUUID().toString();
        event.setCalendarEventId(externalId);
        Assertions.assertEquals(externalId, event.getCalendarEventId());
    }
}