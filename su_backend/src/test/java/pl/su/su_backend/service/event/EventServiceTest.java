package pl.su.su_backend.service.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.su.su_backend.dto.event.EventRequestDto;
import pl.su.su_backend.dto.event.EventResponseDto;
import pl.su.su_backend.dto.event.ParticipantResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.model.enums.EventParticipantRole;
import pl.su.su_backend.model.enums.EventStatus;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.event.Event;
import pl.su.su_backend.model.event.EventParticipant;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.event.EventParticipantRepository;
import pl.su.su_backend.repositories.event.EventRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.testsupport.Fixtures;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventParticipantRepository participantRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private ActivityLogService activityLogService;
    @Mock
    private CalendarService calendarService;
    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private EventService eventService;

    private Users testUser;
    private EventRequestDto testEventRequestDto;
    private Event savedEvent;

    @BeforeEach
    void setUp() {
        testUser = Fixtures.user("Test User", "test@test.com");
        testUser.setId(UUID.randomUUID());

        testEventRequestDto = Fixtures.eventRequestDto("Test Event", "Test description",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));

        savedEvent = Fixtures.eventWithCreator("Test Event", "Test description",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), testUser);
        savedEvent.setId(UUID.randomUUID());
        savedEvent.setStatus(EventStatus.APPROVED);
    }

    @Test
    void createEvent_ShouldCreateSuccessfully_WhenValidData() {
        // Given
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_CREATE)).thenReturn(true);
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);
        when(participantRepository.save(any(EventParticipant.class))).thenReturn(new EventParticipant());
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));
        when(participantRepository.existsByEvent_IdAndUser_Id(savedEvent.getId(), testUser.getId())).thenReturn(false);

        // When
        EventResponseDto result = eventService.createEvent(testEventRequestDto, testUser.getId(), "access-token");

        // Then
        assertNotNull(result);
        verify(usersRepository, times(2)).findById(testUser.getId());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.EVENT_CREATE);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEvent_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () -> 
            eventService.createEvent(testEventRequestDto, testUser.getId(), "access-token"));
        verify(usersRepository).findById(testUser.getId());
    }

    @Test
    void createEvent_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_CREATE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            eventService.createEvent(testEventRequestDto, testUser.getId(), "access-token"));
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.EVENT_CREATE);
    }

    @Test
    void getAllEvents_ShouldReturnAllEvents_WhenHasPermission() {
        // Given
        Event event1 = Fixtures.simpleEvent("Event 1", "Description 1", 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        Event event2 = Fixtures.simpleEvent("Event 2", "Description 2", 
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(2));
        
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW)).thenReturn(true);
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_APPROVE)).thenReturn(true);
        when(eventRepository.findAllByOrderByStartDateAsc()).thenReturn(List.of(event1, event2));

        // When
        List<EventResponseDto> result = eventService.getAllEvents(testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW);
    }

    @Test
    void getAllEvents_ShouldReturnOnlyApproved_WhenNoSUPermission() {
        // Given
        Event approvedEvent = Fixtures.simpleEvent("Approved Event", "Description", 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        approvedEvent.setStatus(EventStatus.APPROVED);
        
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW)).thenReturn(true);
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_APPROVE)).thenReturn(false);
        when(eventRepository.findByStatusOrderByStartDateAsc(EventStatus.APPROVED)).thenReturn(List.of(approvedEvent));

        // When
        List<EventResponseDto> result = eventService.getAllEvents(testUser.getEmail());

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.EVENT_APPROVE);
    }

    @Test
    void getAllEvents_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW)).thenReturn(false);

        // When & Then
        assertThrows(ApiException.class, () -> 
            eventService.getAllEvents(testUser.getEmail()));
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW);
    }

    @Test
    void getEventById_ShouldReturnEvent_WhenExistsAndHasPermission() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW)).thenReturn(true);
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));

        // When
        EventResponseDto result = eventService.getEventById(savedEvent.getId(), testUser.getEmail());

        // Then
        assertNotNull(result);
        verify(usersRepository).findByEmail(testUser.getEmail());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW);
        verify(eventRepository).findById(savedEvent.getId());
    }

    @Test
    void getEventById_ShouldThrowException_WhenEventNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW)).thenReturn(true);
        when(eventRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () -> 
            eventService.getEventById(nonExistentId, testUser.getEmail()));
        verify(eventRepository).findById(nonExistentId);
    }

    @Test
    void getEventById_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW)).thenReturn(false);

        // When & Then
        assertThrows(ApiException.class, () -> 
            eventService.getEventById(savedEvent.getId(), testUser.getEmail()));
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW);
    }

    @Test
    void getEventById_ShouldThrowException_WhenDraftAndNoSUPermission() {
        // Given
        savedEvent.setStatus(EventStatus.DRAFT);
        when(usersRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW)).thenReturn(true);
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_APPROVE)).thenReturn(false);
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));

        // When & Then
        assertThrows(ApiException.class, () -> 
            eventService.getEventById(savedEvent.getId(), testUser.getEmail()));
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.EVENT_APPROVE);
    }

    @Test
    void updateEvent_ShouldUpdateSuccessfully_WhenOwnerOrHasPermission() {
        // Given
        EventRequestDto updateDto = Fixtures.eventRequestDto("Updated Event", "Updated description", 
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(3));
        
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // When
        EventResponseDto result = eventService.updateEvent(savedEvent.getId(), updateDto, testUser.getId(), "access-token");

        // Then
        assertNotNull(result);
        verify(eventRepository).findById(savedEvent.getId());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEvent_ShouldThrowException_WhenEventNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(eventRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () -> 
            eventService.updateEvent(nonExistentId, testEventRequestDto, testUser.getId(), "access-token"));
        verify(eventRepository).findById(nonExistentId);
    }

    @Test
    void updateEvent_ShouldThrowException_WhenNoPermission() {
        // Given
        Users otherUser = Fixtures.user("Other User", "other@test.com");
        otherUser.setId(UUID.randomUUID());
        
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));
        when(permissionService.hasPermission(otherUser.getId(), PermissionCode.EVENT_EDIT)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            eventService.updateEvent(savedEvent.getId(), testEventRequestDto, otherUser.getId(), "access-token"));
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(permissionService).hasPermission(otherUser.getId(), PermissionCode.EVENT_EDIT);
    }

    @Test
    void deleteEvent_ShouldDeleteSuccessfully_WhenOwnerOrHasPermission() {
        // Given
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));

        // When
        eventService.deleteEvent(savedEvent.getId(), testUser.getId(), "access-token");

        // Then
        verify(eventRepository).findById(savedEvent.getId());
        verify(eventRepository).delete(savedEvent);
    }

    @Test
    void deleteEvent_ShouldThrowException_WhenEventNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(eventRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () -> 
            eventService.deleteEvent(nonExistentId, testUser.getId(), "access-token"));
        verify(eventRepository).findById(nonExistentId);
    }

    @Test
    void deleteEvent_ShouldThrowException_WhenNoPermission() {
        // Given
        Users otherUser = Fixtures.user("Other User", "other@test.com");
        otherUser.setId(UUID.randomUUID());
        
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));
        when(permissionService.hasPermission(otherUser.getId(), PermissionCode.EVENT_DELETE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            eventService.deleteEvent(savedEvent.getId(), otherUser.getId(), "access-token"));
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(permissionService).hasPermission(otherUser.getId(), PermissionCode.EVENT_DELETE);
    }

    @Test
    void addParticipant_ShouldAddSuccessfully_WhenValidData() {
        // Given
        Users participant = Fixtures.user("Participant", "participant@test.com");
        participant.setId(UUID.randomUUID());
        
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));
        when(usersRepository.findById(participant.getId())).thenReturn(Optional.of(participant));
        when(participantRepository.existsByEvent_IdAndUser_Id(savedEvent.getId(), participant.getId())).thenReturn(false);
        when(participantRepository.save(any(EventParticipant.class))).thenReturn(new EventParticipant());

        // When
        ParticipantResponseDto result = eventService.addParticipant(savedEvent.getId(), participant.getId(), 
                EventParticipantRole.PARTICIPANT, true);

        // Then
        assertNotNull(result);
        verify(eventRepository).findById(savedEvent.getId());
        verify(usersRepository).findById(participant.getId());
        verify(participantRepository).save(any(EventParticipant.class));
    }

    @Test
    void addParticipant_ShouldThrowException_WhenEventNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(eventRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () -> 
            eventService.addParticipant(nonExistentId, testUser.getId(), EventParticipantRole.PARTICIPANT, true));
        verify(eventRepository).findById(nonExistentId);
    }

    @Test
    void addParticipant_ShouldThrowException_WhenUserNotFound() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));
        when(usersRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () -> 
            eventService.addParticipant(savedEvent.getId(), nonExistentUserId, EventParticipantRole.PARTICIPANT, true));
        verify(usersRepository).findById(nonExistentUserId);
    }

    @Test
    void addParticipant_ShouldThrowException_WhenAlreadyParticipant() {
        // Given
        Users participant = Fixtures.user("Participant", "participant@test.com");
        participant.setId(UUID.randomUUID());
        
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));
        when(usersRepository.findById(participant.getId())).thenReturn(Optional.of(participant));
        when(participantRepository.existsByEvent_IdAndUser_Id(savedEvent.getId(), participant.getId())).thenReturn(true);

        // When & Then
        assertThrows(ApiException.class, () -> 
            eventService.addParticipant(savedEvent.getId(), participant.getId(), EventParticipantRole.PARTICIPANT, true));
        verify(participantRepository).existsByEvent_IdAndUser_Id(savedEvent.getId(), participant.getId());
    }

    @Test
    void removeParticipant_ShouldRemoveSuccessfully_WhenValidData() {
        // Given
        Users participant = Fixtures.user("Participant", "participant@test.com");
        participant.setId(UUID.randomUUID());
        
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));

        // When
        eventService.removeParticipant(savedEvent.getId(), participant.getId(), testUser.getId());

        // Then
        verify(participantRepository).deleteByEvent_IdAndUser_Id(savedEvent.getId(), participant.getId());
    }

    @Test
    void removeParticipant_ShouldThrowException_WhenNoPermission() {
        // Given
        Users otherUser = Fixtures.user("Other User", "other@test.com");
        otherUser.setId(UUID.randomUUID());
        Users participant = Fixtures.user("Participant", "participant@test.com");
        participant.setId(UUID.randomUUID());
        
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));
        when(permissionService.hasPermission(otherUser.getId(), PermissionCode.EVENT_EDIT)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            eventService.removeParticipant(savedEvent.getId(), participant.getId(), otherUser.getId()));
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(permissionService).hasPermission(otherUser.getId(), PermissionCode.EVENT_EDIT);
    }

    @Test
    void approveEvent_ShouldApproveSuccessfully_WhenValidEvent() {
        // Given
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_APPROVE)).thenReturn(true);
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // When
        EventResponseDto result = eventService.approveEvent(savedEvent.getId(), testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(EventStatus.APPROVED, savedEvent.getStatus());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.EVENT_APPROVE);
        verify(eventRepository).findById(savedEvent.getId());
        verify(eventRepository).save(savedEvent);
    }

    @Test
    void approveEvent_ShouldThrowException_WhenEventNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_APPROVE)).thenReturn(true);
        when(eventRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () -> 
            eventService.approveEvent(nonExistentId, testUser.getId()));
        verify(eventRepository).findById(nonExistentId);
    }

    @Test
    void rejectEvent_ShouldRejectSuccessfully_WhenValidEvent() {
        // Given
        when(permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_APPROVE)).thenReturn(true);
        when(eventRepository.findById(savedEvent.getId())).thenReturn(Optional.of(savedEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);

        // When
        EventResponseDto result = eventService.rejectEvent(savedEvent.getId(), testUser.getId());

        // Then
        assertNotNull(result);
        assertEquals(EventStatus.REJECTED, savedEvent.getStatus());
        verify(permissionService).hasPermission(testUser.getId(), PermissionCode.EVENT_APPROVE);
        verify(eventRepository).findById(savedEvent.getId());
        verify(eventRepository).save(savedEvent);
    }

    @Test
    void getUpcomingEvents_ShouldReturnOnlyApprovedAndNotEndedEvents() {
        // Given
        Event upcomingEvent = Fixtures.simpleEvent("Upcoming Event", "Description", 
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        upcomingEvent.setStatus(EventStatus.APPROVED);
        
        Event endedEvent = Fixtures.simpleEvent("Ended Event", "Description", 
                LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(1));
        endedEvent.setStatus(EventStatus.APPROVED);
        
        Event draftEvent = Fixtures.simpleEvent("Draft Event", "Description", 
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(2));
        draftEvent.setStatus(EventStatus.DRAFT);
        
        when(eventRepository.findByStatusAndEndDateGreaterThanOrderByStartDateAsc(
                eq(EventStatus.APPROVED), any(LocalDateTime.class))).thenReturn(List.of(upcomingEvent));

        // When
        List<EventResponseDto> result = eventService.getUpcomingEvents();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Upcoming Event", result.getFirst().getTitle());
        verify(eventRepository).findByStatusAndEndDateGreaterThanOrderByStartDateAsc(
                eq(EventStatus.APPROVED), any(LocalDateTime.class));
    }
}
