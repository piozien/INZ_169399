package pl.su.su_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.su.su_backend.BaseIntegrationTest;
import pl.su.su_backend.dto.auth.LoginRequestDto;
import pl.su.su_backend.dto.user.UserRequestDto;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.JwtService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        UserRequestDto requestDto = UserRequestDto.builder()
                .fullName("Test User")
                .email("usertest@school.edu")
                .password("Password123!")
                .authProvider(AuthProvider.LOCAL)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("usertest@school.edu"))
                .andExpect(jsonPath("$.fullName").value("Test User"));

        var savedUser = userRepository.findByEmail("usertest@school.edu");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getFullName()).isEqualTo("Test User");
        assertThat(savedUser.get().getPassword()).isNotEqualTo("Password123!");
    }

    @Test
    void shouldRejectRegistrationWhenEmailIsAlreadyUsed() throws Exception {
        UserRequestDto requestDto = UserRequestDto.builder()
                .fullName("Admin2")
                .email("admin@school.edu")
                .password("Password123!")
                .authProvider(AuthProvider.LOCAL)
                .build();

        long initialUserCount = userRepository.count();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("CONFLICT"))
                .andExpect(jsonPath("$.detail").value("Adres e-mail jest już używany."));

        assertThat(userRepository.count()).isEqualTo(initialUserCount);

        assertThat(userRepository.findByEmail("Admin2")).isEmpty();
    }

    @Test
    void shouldRejectRegistrationWhenCredentialsIsWrong() throws Exception {
        UserRequestDto requestDto = UserRequestDto.builder()
                .fullName("")
                .email("")
                .password("")
                .authProvider(AuthProvider.LOCAL)
                .build();

        long initialUserCount = userRepository.count();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.detail").value("Walidacja nie powiodła się"));

            assertThat(userRepository.count()).isEqualTo(initialUserCount);
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequestDto loginRequestDto = LoginRequestDto.builder()
                .email("admin@school.edu")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.email").value("admin@school.edu"))
                .andExpect(jsonPath("$.fullName").value("Administrator Systemu"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))

                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles").value(hasSize(1)))
                .andExpect(jsonPath("$.roles").value(containsInAnyOrder("ADMINISTRATOR")))

                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }

    @Test
    void shouldRejectLoginWhenCredentialsIsWrong() throws Exception {
        LoginRequestDto invalidCredentials = LoginRequestDto.builder()
                .email("test@school.edu")
                .password("wrongPassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCredentials)))

                .andExpect(status().isUnauthorized())

                .andExpect(jsonPath("$.title").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.detail").value("Błędny email lub hasło"));

    }


    @Test
    void shouldRotateRefreshToken() throws Exception {
        String userEmail = "admin@school.edu";
        String oldRefreshToken = jwtService.generateRefreshToken(userEmail);
        Cookie requestCookie = new Cookie("refreshToken", oldRefreshToken);

        Thread.sleep(1000);

        MvcResult result = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(requestCookie))

                .andExpect(status().isOk())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        Cookie responseCookie = result.getResponse().getCookie("refreshToken");

        assertThat(responseCookie).isNotNull();

        String newRefreshToken = responseCookie.getValue();

        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);
        assertThat(jwtService.extractEmail(newRefreshToken)).isEqualTo(userEmail);
    }

    @Test
    void shouldRejectRefreshWhenCookieIsMissing() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Brak tokenu odświeżającego w ciasteczkach"));
    }

}