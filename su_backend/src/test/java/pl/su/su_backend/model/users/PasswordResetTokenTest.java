package pl.su.su_backend.model.users;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.testsupport.Fixtures;

import java.time.LocalDateTime;
import java.util.UUID;

public class PasswordResetTokenTest {

    @Test
    void builderSetsAllFields() {
        UUID id = UUID.randomUUID();
        String token = "test-token";
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        Boolean used = false;
        LocalDateTime createdAt = LocalDateTime.now();

        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .id(id)
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .used(used)
                .createdAt(createdAt)
                .build();

        Assertions.assertEquals(id, passwordResetToken.getId());
        Assertions.assertEquals(token, passwordResetToken.getToken());
        Assertions.assertEquals(user, passwordResetToken.getUser());
        Assertions.assertEquals(expiresAt, passwordResetToken.getExpiresAt());
        Assertions.assertEquals(used, passwordResetToken.getUsed());
        Assertions.assertEquals(createdAt, passwordResetToken.getCreatedAt());
    }

    @Test
    void canChangeFieldsViaSetters() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        UUID id = UUID.randomUUID();
        String token = "test-token";
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(3);
        Boolean used = false;
        LocalDateTime createdAt = LocalDateTime.now();

        passwordResetToken.setId(id);
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiresAt(expiresAt);
        passwordResetToken.setUsed(used);
        passwordResetToken.setCreatedAt(createdAt);

        Assertions.assertEquals(id, passwordResetToken.getId());
        Assertions.assertEquals(token, passwordResetToken.getToken());
        Assertions.assertEquals(user, passwordResetToken.getUser());
        Assertions.assertEquals(expiresAt, passwordResetToken.getExpiresAt());
        Assertions.assertEquals(used, passwordResetToken.getUsed());
        Assertions.assertEquals(createdAt, passwordResetToken.getCreatedAt());
    }

    @Test
    void hasCorrectDefaultValues() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();

        Assertions.assertNull(passwordResetToken.getId());
        Assertions.assertNull(passwordResetToken.getToken());
        Assertions.assertNull(passwordResetToken.getUser());
        Assertions.assertNull(passwordResetToken.getExpiresAt());
        Assertions.assertFalse(passwordResetToken.getUsed());
        Assertions.assertNull(passwordResetToken.getCreatedAt());
    }

    @Test
    void onCreateSetsCreatedAt() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();

        passwordResetToken.onCreate();

        Assertions.assertNotNull(passwordResetToken.getCreatedAt());
    }

    @Test
    void isValidReturnsTrueForValidToken() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUsed(false);
        passwordResetToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        boolean isValid = passwordResetToken.isValid();

        Assertions.assertTrue(isValid);
    }

    @Test
    void isValidReturnsFalseForUsedToken() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUsed(true);
        passwordResetToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        boolean isValid = passwordResetToken.isValid();

        Assertions.assertFalse(isValid);
    }

    @Test
    void isValidReturnsFalseForExpiredToken() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUsed(false);
        passwordResetToken.setExpiresAt(LocalDateTime.now().minusHours(1));

        boolean isValid = passwordResetToken.isValid();

        Assertions.assertFalse(isValid);
    }

    @Test
    void isValidThrowsExceptionForNullUsedToken() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUsed(null);
        passwordResetToken.setExpiresAt(LocalDateTime.now().plusHours(1));

        Assertions.assertThrows(NullPointerException.class, () -> passwordResetToken.isValid());
    }

    @Test
    void isValidThrowsExceptionForNullExpiresAtToken() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUsed(false);
        passwordResetToken.setExpiresAt(null);

        Assertions.assertThrows(NullPointerException.class, () -> passwordResetToken.isValid());
    }

    @Test
    void markAsUsedSetsUsedToTrue() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUsed(false);

        passwordResetToken.markAsUsed();

        Assertions.assertTrue(passwordResetToken.getUsed());
    }

    @Test
    void canHandleNullValues() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();

        passwordResetToken.setToken(null);
        passwordResetToken.setUser(null);
        passwordResetToken.setExpiresAt(null);
        passwordResetToken.setUsed(null);
        passwordResetToken.setCreatedAt(null);

        Assertions.assertNull(passwordResetToken.getToken());
        Assertions.assertNull(passwordResetToken.getUser());
        Assertions.assertNull(passwordResetToken.getExpiresAt());
        Assertions.assertNull(passwordResetToken.getUsed());
        Assertions.assertNull(passwordResetToken.getCreatedAt());
    }

    @Test
    void canHandleEmptyStringToken() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();

        passwordResetToken.setToken("");

        Assertions.assertEquals("", passwordResetToken.getToken());
    }

    @Test
    void canHandleSpecialCharactersInToken() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        String tokenWithSpecialChars = "@#$%^&*()";

        passwordResetToken.setToken(tokenWithSpecialChars);

        Assertions.assertEquals(tokenWithSpecialChars, passwordResetToken.getToken());
    }

    @Test
    void canHandleLongToken() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        String longToken = "A".repeat(1000);

        passwordResetToken.setToken(longToken);

        Assertions.assertEquals(longToken, passwordResetToken.getToken());
    }

    @Test
    void canHandleExpiresAtInThePast() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        passwordResetToken.setExpiresAt(pastTime);

        Assertions.assertEquals(pastTime, passwordResetToken.getExpiresAt());
        Assertions.assertFalse(passwordResetToken.isValid());
    }

    @Test
    void canHandleExpiresAtInTheFuture() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

        passwordResetToken.setExpiresAt(futureTime);

        Assertions.assertEquals(futureTime, passwordResetToken.getExpiresAt());
    }

    @Test
    void canHandleCreatedAtInThePast() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        passwordResetToken.setCreatedAt(pastTime);

        Assertions.assertEquals(pastTime, passwordResetToken.getCreatedAt());
    }

    @Test
    void canHandleCreatedAtInTheFuture() {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

        passwordResetToken.setCreatedAt(futureTime);

        Assertions.assertEquals(futureTime, passwordResetToken.getCreatedAt());
    }
}
