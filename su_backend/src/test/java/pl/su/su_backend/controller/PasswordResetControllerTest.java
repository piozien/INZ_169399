package pl.su.su_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.su.su_backend.BaseIntegrationTest;
import pl.su.su_backend.dto.auth.PasswordResetConfirmDto;
import pl.su.su_backend.dto.auth.PasswordResetRequestDto;
import pl.su.su_backend.model.users.PasswordResetToken;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.auth.PasswordResetTokenRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.user.MailService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PasswordResetControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private MailSender mailSender;
    @MockitoBean
    private MailService mailService;

    @Test
    void shouldCompletePasswordResetFlowSuccessfully() throws Exception {
        String email = "admin@school.edu";
        doNothing().when(mailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        PasswordResetRequestDto requestDto = new PasswordResetRequestDto();
        requestDto.setEmail(email);

        String oldPasswordHash = usersRepository.findByEmail(email).get().getPassword();

        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        PasswordResetToken savedToken = passwordResetTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Token nie został zapisany w bazie!"));

        assertThat(savedToken.getToken()).isNotNull();
        assertThat(savedToken.isValid()).isTrue();

        verify(mailService).sendPasswordResetEmail(anyString(), anyString(), anyString());

        mockMvc.perform(get("/api/auth/password-reset/validate/" + savedToken.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        String newPassword = "NewSuperPassword123!";
        PasswordResetConfirmDto confirmDto = new PasswordResetConfirmDto(savedToken.getToken(), newPassword);

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmDto)))
                .andExpect(status().isOk());

        Users updatedUser = usersRepository.findByEmail(email).get();

        assertThat(updatedUser.getPassword()).isNotEqualTo(oldPasswordHash);
        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();

        PasswordResetToken usedToken = passwordResetTokenRepository.findById(savedToken.getId()).get();
        assertThat(usedToken.isUsed()).isTrue();
    }

    @Test
    void shouldRejectPasswordChangeWithInvalidToken() throws Exception {
        PasswordResetConfirmDto confirmDto = new PasswordResetConfirmDto("lorem ipsum", "NewPass!");

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Nieprawidłowy lub przestarzały token"));
    }

    @Test
    void shouldRejectPasswordChangeWhenUserStatusIsBlocked() throws Exception {
        String blockedUser = "student7@school.edu"; //blocked user from migration
        PasswordResetRequestDto requestDto = new PasswordResetRequestDto();

        requestDto.setEmail(blockedUser);

        long initialTokenCount = passwordResetTokenRepository.count();

        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Twoje konto jest zablokowane." +
                        " Nie możesz zresetować hasła."));

        long tokenCount = passwordResetTokenRepository.count();
        assertThat(tokenCount).isEqualTo(initialTokenCount);
    }
}

