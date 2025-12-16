package pl.su.su_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.BaseIntegrationTest;
import pl.su.su_backend.dto.event.EventRequestDto;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.event.Event;
import pl.su.su_backend.model.enums.EventParticipantRole;
import pl.su.su_backend.model.enums.EventStatus;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.event.EventParticipantRepository;
import pl.su.su_backend.repositories.event.EventRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.JwtService;
import pl.su.su_backend.service.user.MailService;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class EventControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventParticipantRepository participantRepository;

    @Autowired
    private CouncilRepository councilRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EntityManager entityManager;

    @MockitoBean
    private MailService mailService;

    private final String ADMIN_EMAIL = "admin@school.edu";
    private final String CHAIRMAN_EMAIL = "student1@school.edu";
    private final String STUDENT_EMAIL = "student6@school.edu";


    @Test
    void shouldCreateEventSuccessfully() throws Exception {
        Cookie authCookie = generateAuthCookie(CHAIRMAN_EMAIL, "Uczen");
        UUID councilId = getCouncilIdByCode("SU2025");

        EventRequestDto requestDto = EventRequestDto.builder()
                .title("Test Event")
                .description("Description!")
                .location("Gym")
                .startDate(LocalDateTime.now().plusDays(7))
                .endDate(LocalDateTime.now().plusDays(7).plusHours(4))
                .councilId(councilId)
                .build();

        mockMvc.perform(post("/api/events")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Event"))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        boolean exists = eventRepository.findAll().stream()
                .anyMatch(e -> e.getTitle().equals("Test Event"));
        assertThat(exists).isTrue();
    }

    @Test
    void shouldRejectEventWithInvalidDates() throws Exception {
        Cookie authCookie = generateAuthCookie(CHAIRMAN_EMAIL, "Przewodniczacy");
        UUID councilId = getCouncilIdByCode("SU2025");

        EventRequestDto requestDto = EventRequestDto.builder()
                .title("Incorrect Event")
                .description("Description")
                .location("Class")
                .startDate(LocalDateTime.now().plusDays(2))
                .endDate(LocalDateTime.now().plusDays(1))
                .councilId(councilId)
                .build();

        mockMvc.perform(post("/api/events")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void shouldJoinFutureEventSuccessfully() throws Exception {
        Event event = createEventInDb("Upcoming Event", LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(6));
        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");

        mockMvc.perform(post("/api/events/{eventId}/participants/join", event.getId())
                        .cookie(studentCookie)
                        .param("role", EventParticipantRole.PARTICIPANT.name())
                        .param("confirmed", "true"))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        UUID userId = getUserIdByEmail(STUDENT_EMAIL);
        boolean isParticipant = participantRepository.existsByEvent_IdAndUser_Id(event.getId(), userId);
        assertThat(isParticipant).isTrue();
    }

    @Test
    void shouldRejectJoinForPastEvent() throws Exception {
        Event pastEvent = createEventInDb("Historical Event", LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(9));
        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Spóźnialski");

        mockMvc.perform(post("/api/events/{eventId}/participants/join", pastEvent.getId())
                        .cookie(studentCookie)
                        .param("role", EventParticipantRole.PARTICIPANT.name()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Nie można zapisać się na wydarzenie, które już się zakończyło"));
    }

    @Test
    void shouldApproveEventByAdmin() throws Exception {
        Event pendingEvent = createEventInDb("Upcoming Event", LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(11));
        pendingEvent.setStatus(EventStatus.PENDING);
        eventRepository.save(pendingEvent);

        Cookie adminCookie = generateAuthCookie(ADMIN_EMAIL, "Admin");

        mockMvc.perform(put("/api/events/{eventId}/approve", pendingEvent.getId())
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        Event updatedEvent = eventRepository.findById(pendingEvent.getId()).orElseThrow();
        assertThat(updatedEvent.getStatus()).isEqualTo(EventStatus.APPROVED);
    }

    @Test
    void shouldForbidStudentFromApprovingEvent() throws Exception {
        Event pendingEvent = createEventInDb("Upcoming Event", LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(11));
        pendingEvent.setStatus(EventStatus.PENDING);
        eventRepository.save(pendingEvent);

        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");

        mockMvc.perform(put("/api/events/{eventId}/approve", pendingEvent.getId())
                        .cookie(studentCookie))
                .andExpect(status().isForbidden());

        Event unchangedEvent = eventRepository.findById(pendingEvent.getId()).orElseThrow();
        assertThat(unchangedEvent.getStatus()).isEqualTo(EventStatus.PENDING);
    }


    private Event createEventInDb(String title, LocalDateTime start, LocalDateTime end) {
        Users creator = usersRepository.findByEmail(CHAIRMAN_EMAIL).orElseThrow();
        Council council = councilRepository.findByJoinCode("SU2025").orElseThrow();

        Event event = Event.builder()
                .title(title)
                .description("Test description")
                .location("School")
                .startDate(start)
                .endDate(end)
                .council(council)
                .createdBy(creator)
                .status(EventStatus.APPROVED)
                .maxParticipants(50)
                .build();
        return eventRepository.save(event);
    }

    private Cookie generateAuthCookie(String email, String fullName) {
        String token = jwtService.generateToken(email, fullName);
        return new Cookie("accessToken", token);
    }

    private UUID getUserIdByEmail(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    private UUID getCouncilIdByCode(String code) {
        return councilRepository.findByJoinCode(code)
                .orElseThrow(() -> new RuntimeException("Council not found"))
                .getId();
    }
}