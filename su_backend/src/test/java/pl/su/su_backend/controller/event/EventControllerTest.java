package pl.su.su_backend.controller.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.flywaydb.core.Flyway;
import pl.su.su_backend.config.JwtConfig;
import pl.su.su_backend.dto.event.EventRequestDto;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.EventStatus;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.event.Event;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.event.EventRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.repositories.permission.PermissionRepository;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.testsupport.Fixtures;
import pl.su.su_backend.testsupport.OAuth2TestConfig;
import pl.su.su_backend.testsupport.RolePermissionTestHelper;
import pl.su.su_backend.model.enums.EventParticipantRole;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(OAuth2TestConfig.class)
class EventControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private PermissionRepository permissionRepository;

    private Users teacherUser;
    private Users supervisorUser;
    private Users studentUser;
    private String teacherToken;
    private String supervisorToken;
    private String studentToken;
    private Event testEvent;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUp() {
        flyway.migrate();

        userRoleRepository.deleteAll();
        eventRepository.deleteAll();
        usersRepository.deleteAll();

        Role teacherRole = RolePermissionTestHelper.ensureRole(roleRepository, permissionRepository, RoleCode.NAUCZYCIEL,
                PermissionCode.EVENT_VIEW,
                PermissionCode.EVENT_CREATE,
                PermissionCode.EVENT_EDIT,
                PermissionCode.EVENT_DELETE);
        Role supervisorRole = RolePermissionTestHelper.ensureRole(roleRepository, permissionRepository, RoleCode.OPIEKUN_SU,
                PermissionCode.EVENT_VIEW,
                PermissionCode.EVENT_VIEW_DRAFTS,
                PermissionCode.EVENT_CREATE,
                PermissionCode.EVENT_EDIT,
                PermissionCode.EVENT_DELETE,
                PermissionCode.EVENT_APPROVE);
        Role studentRole = RolePermissionTestHelper.ensureRole(roleRepository, permissionRepository, RoleCode.UCZEN,
                PermissionCode.EVENT_VIEW);

        teacherUser = Fixtures.createUserWithRole(usersRepository, userRoleRepository,
                "Teacher", "teacher@test.local", StatusEnum.CONFIRMED, AuthProvider.LOCAL, teacherRole);
        supervisorUser = Fixtures.createUserWithRole(usersRepository, userRoleRepository,
                "Supervisor", "supervisor@test.local", StatusEnum.CONFIRMED, AuthProvider.LOCAL, supervisorRole);
        studentUser = Fixtures.createUserWithRole(usersRepository, userRoleRepository,
                "Student", "student@test.local", StatusEnum.CONFIRMED, AuthProvider.LOCAL, studentRole);

        teacherToken = jwtConfig.generateToken(teacherUser.getEmail());
        supervisorToken = jwtConfig.generateToken(supervisorUser.getEmail());
        studentToken = jwtConfig.generateToken(studentUser.getEmail());

        testEvent = Fixtures.eventNoId("Test Event", "Test Description",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        testEvent.setCreatedBy(teacherUser);
        testEvent.setStatus(EventStatus.APPROVED);
        testEvent = eventRepository.save(testEvent);
        eventRepository.flush();
    }

    @Test
    void createEvent_ShouldReturnCreated_WhenNauczycielHasPermission() {
        EventRequestDto request = Fixtures.eventRequestDto(
                "New Event",
                "Event description",
                LocalDateTime.of(2025, 12, 10, 10, 0),
                LocalDateTime.of(2025, 12, 10, 12, 0)
        );
        request.setLocation("Room 101");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/events",
                Fixtures.httpEntityWithToken(teacherToken, request),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("New Event");
    }

    @Test
    void createEvent_ShouldReturnCreated_WhenOpiekunHasPermission() {
        EventRequestDto request = Fixtures.eventRequestDto(
                "Supervisor Event",
                "Description",
                LocalDateTime.of(2025, 12, 10, 10, 0),
                LocalDateTime.of(2025, 12, 10, 12, 0)
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/events",
                Fixtures.httpEntityWithToken(supervisorToken, request),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).contains("Supervisor Event");
    }

    @Test
    void createEvent_ShouldReturnForbidden_WhenUczenNoPermission() {
        EventRequestDto request = Fixtures.eventRequestDto(
                "Student Event",
                "Description",
                LocalDateTime.of(2025, 12, 10, 10, 0),
                LocalDateTime.of(2025, 12, 10, 12, 0)
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/events",
                Fixtures.httpEntityWithToken(studentToken, request),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getAllEvents_ShouldReturnOk_WhenUczenHasPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(studentToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllEvents_ShouldReturnOk_WhenNauczycielHasPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(teacherToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Test Event");
    }

    @Test
    void getEventById_ShouldReturnOk_WhenUczenHasPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/" + testEvent.getId(),
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(studentToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Test Event");
    }

    @Test
    void updateEvent_ShouldReturnOk_WhenNauczycielHasPermission() {
        EventRequestDto request = Fixtures.eventRequestDto(
                "Updated Event",
                "New description",
                LocalDateTime.of(2025, 12, 10, 10, 0),
                LocalDateTime.of(2025, 12, 10, 12, 0)
        );

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/" + testEvent.getId(),
                HttpMethod.PUT,
                Fixtures.httpEntityWithToken(teacherToken, request),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Updated Event");
    }

    @Test
    void updateEvent_ShouldReturnForbidden_WhenUczenNoPermission() {
        EventRequestDto request = Fixtures.eventRequestDto(
                "Update Attempt",
                "Description",
                LocalDateTime.of(2025, 12, 10, 10, 0),
                LocalDateTime.of(2025, 12, 10, 12, 0)
        );

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/" + testEvent.getId(),
                HttpMethod.PUT,
                Fixtures.httpEntityWithToken(studentToken, request),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void deleteEvent_ShouldReturnNoContent_WhenNauczycielHasPermission() {
        Event eventToDelete = Fixtures.eventNoId("Event to Delete", "Description",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
        eventToDelete.setCreatedBy(teacherUser);
        eventToDelete.setStatus(EventStatus.APPROVED);
        eventToDelete = eventRepository.save(eventToDelete);
        eventRepository.flush();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/" + eventToDelete.getId(),
                HttpMethod.DELETE,
                Fixtures.httpEntityWithToken(teacherToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteEvent_ShouldReturnForbidden_WhenUczenNoPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/" + testEvent.getId(),
                HttpMethod.DELETE,
                Fixtures.httpEntityWithToken(studentToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void approveEvent_ShouldReturnOk_WhenOpiekunHasPermission() {
        testEvent.setStatus(EventStatus.PENDING);
        testEvent = eventRepository.save(testEvent);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/" + testEvent.getId() + "/approve",
                HttpMethod.PUT,
                Fixtures.httpEntityWithToken(supervisorToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void approveEvent_ShouldReturnForbidden_WhenNauczycielNoPermission() {
        testEvent.setStatus(EventStatus.PENDING);
        testEvent = eventRepository.save(testEvent);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/" + testEvent.getId() + "/approve",
                HttpMethod.PUT,
                Fixtures.httpEntityWithToken(teacherToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getPendingEvents_ShouldReturnOk_WhenOpiekunHasPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/pending",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(supervisorToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getPendingEvents_ShouldReturnForbidden_WhenUczenNoPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/pending",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(studentToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void getUpcomingEvents_ShouldReturnOk_PublicEndpoint() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/upcoming",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(studentToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getEventsInDateRange_ShouldReturnOk_WhenUczenHasPermission() {
        String start = LocalDateTime.now().minusDays(1).withNano(0).toString();
        String end = LocalDateTime.now().plusDays(2).withNano(0).toString();

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/range?startDate=" + start + "&endDate=" + end,
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(studentToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void addParticipant_ShouldReturnOk() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/events/" + testEvent.getId() + "/participants/" + studentUser.getId() + "?role=" + EventParticipantRole.PARTICIPANT + "&confirmed=true",
                Fixtures.httpEntityWithToken(teacherToken, ""),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getEventParticipants_ShouldReturnOk() {
        // ensure at least one participant exists
        restTemplate.postForEntity(
                "/api/events/" + testEvent.getId() + "/participants/" + studentUser.getId() + "?role=" + EventParticipantRole.PARTICIPANT + "&confirmed=true",
                Fixtures.httpEntityWithToken(teacherToken, ""),
                String.class
        );

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/" + testEvent.getId() + "/participants",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(teacherToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void removeParticipant_ShouldReturnNoContent_WhenEditorHasPermission() {
        // add first
        restTemplate.postForEntity(
                "/api/events/" + testEvent.getId() + "/participants/" + studentUser.getId() + "?role=" + EventParticipantRole.PARTICIPANT + "&confirmed=true",
                Fixtures.httpEntityWithToken(teacherToken, ""),
                String.class
        );

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/" + testEvent.getId() + "/participants/" + studentUser.getId(),
                HttpMethod.DELETE,
                Fixtures.httpEntityWithToken(teacherToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getUserEvents_ShouldReturnOk() {
        restTemplate.postForEntity(
                "/api/events/" + testEvent.getId() + "/participants/" + studentUser.getId() + "?role=" + EventParticipantRole.PARTICIPANT + "&confirmed=true",
                Fixtures.httpEntityWithToken(teacherToken, ""),
                String.class
        );

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/user/" + studentUser.getId(),
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(studentToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void rejectEvent_ShouldReturnOk_WhenOpiekunHasPermission() {
        testEvent.setStatus(EventStatus.PENDING);
        testEvent = eventRepository.save(testEvent);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/events/" + testEvent.getId() + "/reject",
                HttpMethod.PUT,
                Fixtures.httpEntityWithToken(supervisorToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

