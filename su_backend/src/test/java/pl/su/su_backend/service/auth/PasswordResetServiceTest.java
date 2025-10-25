package pl.su.su_backend.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.users.PasswordResetToken;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.auth.PasswordResetTokenRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.user.MailService;
import pl.su.su_backend.testsupport.Fixtures;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private MailService mailService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private Users testUser;
    private String testEmail = "test@test.com";

    @BeforeEach
    void setUp() {
        testUser = Fixtures.userWithStatus("Test User", testEmail, StatusEnum.CONFIRMED);
        ReflectionTestUtils.setField(passwordResetService, "expirationHours", 24);
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void sendPasswordResetEmail_ShouldWork_WhenUserExists() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        passwordResetService.sendPasswordResetEmail(testEmail);

        // Then
        verify(usersRepository).findByEmail(testEmail);
        verify(mailService).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void sendPasswordResetEmail_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () -> passwordResetService.sendPasswordResetEmail(testEmail));
    }

    @Test
    void resetPassword_ShouldWork_WhenValidToken() {
        // Given
        String token = "valid-token";
        String newPassword = "newPassword123";
        PasswordResetToken resetToken = Fixtures.passwordResetToken(testUser, token, LocalDateTime.now().plusHours(1));
        
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

        // When
        passwordResetService.resetPassword(token, newPassword);

        // Then
        verify(passwordEncoder).encode(newPassword);
        verify(usersRepository).save(testUser);
    }

    @Test
    void resetPassword_ShouldThrowException_WhenTokenNotFound() {
        // Given
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ApiException.class, () -> passwordResetService.resetPassword("invalid-token", "password"));
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenValid() {
        // Given
        String token = "valid-token";
        PasswordResetToken resetToken = Fixtures.passwordResetToken(testUser, token, LocalDateTime.now().plusHours(1));
        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // When
        boolean result = passwordResetService.isTokenValid(token);

        // Then
        assertTrue(result);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenNotFound() {
        // Given
        when(tokenRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // When
        boolean result = passwordResetService.isTokenValid("invalid-token");

        // Then
        assertFalse(result);
    }
}
