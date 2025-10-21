package pl.su.su_backend.model.users;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.testsupport.Fixtures;

import java.time.LocalDateTime;
import java.util.UUID;

public class UsersTest {

    @Test
    void onCreateWithPrePersist() {
        Users user = Users.builder()
                .createdAt(null)
                .status(null)
                .build();
        Assertions.assertNotNull(user);
        Assertions.assertNull(user.getStatus());
        Assertions.assertNull(user.getCreatedAt());

        user.onCreate(); // activation of the PrePersist callback
        Assertions.assertEquals(StatusEnum.PENDING, user.getStatus());
        Assertions.assertNotNull(user.getCreatedAt());
    }

    @Test
    void onCreateSetsCreatedAtWhenNull() {
        Users user = Users.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .build();

        Assertions.assertNull(user.getCreatedAt());
        user.onCreate();
        Assertions.assertNotNull(user.getCreatedAt());
    }

    @Test
    void onCreateDoesNotOverrideExistingCreatedAt() {
        LocalDateTime fixed = LocalDateTime.of(2025, 10, 17, 10, 0);
        Users user = Users.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .createdAt(fixed)
                .build();

        user.onCreate();
        Assertions.assertEquals(fixed, user.getCreatedAt());
    }

    @Test
    void onCreateSetsDefaultStatusToPending() {
        Users user = Users.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .build();

        user.onCreate();
        Assertions.assertEquals(StatusEnum.PENDING, user.getStatus());
    }

    @Test
    void onCreateDoesNotOverrideExistingStatus() {
        Users user = Users.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .status(StatusEnum.CONFIRMED)
                .build();

        user.onCreate();
        Assertions.assertEquals(StatusEnum.CONFIRMED, user.getStatus());
    }

    @Test
    void isBlockedReturnsTrueWhenStatusIsBlocked() {
        Users user = Users.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .status(StatusEnum.BLOCKED)
                .build();

        Assertions.assertTrue(user.isBlocked());
    }

    @Test
    void isBlockedReturnsFalseWhenStatusIsNotBlocked() {
        Users user = Users.builder()
                .fullName("Test User")
                .email("test@example.com")
                .password("password123")
                .status(StatusEnum.CONFIRMED)
                .build();

        Assertions.assertFalse(user.isBlocked());
    }

    @Test
    void builderSetsAllFields() {
        Classes schoolClass = Fixtures.schoolClass("3A", "2025/26");
        LocalDateTime now = LocalDateTime.now();
        UUID id = UUID.randomUUID();

        Users user = Users.builder()
                .id(id)
                .fullName("John Doe")
                .email("john@example.com")
                .password("password123")
                .status(StatusEnum.CONFIRMED)
                .classes(schoolClass)
                .createdAt(now)
                .authProvider(AuthProvider.MICROSOFT)
                .externalId("microsoft123")
                .refreshToken("refresh123")
                .build();

        Assertions.assertEquals(id, user.getId());
        Assertions.assertEquals("John Doe", user.getFullName());
        Assertions.assertEquals("john@example.com", user.getEmail());
        Assertions.assertEquals("password123", user.getPassword());
        Assertions.assertEquals(StatusEnum.CONFIRMED, user.getStatus());
        Assertions.assertEquals(schoolClass, user.getClasses());
        Assertions.assertEquals(now, user.getCreatedAt());
        Assertions.assertEquals(AuthProvider.MICROSOFT, user.getAuthProvider());
        Assertions.assertEquals("microsoft123", user.getExternalId());
        Assertions.assertEquals("refresh123", user.getRefreshToken());
    }

    @Test
    void canChangeFieldsViaSetters() {
        Users user = new Users();
        Classes schoolClass = Fixtures.schoolClass("2B", "2025/26");
        LocalDateTime now = LocalDateTime.now();

        user.setFullName("Test test");
        user.setEmail("test@example.com");
        user.setPassword("newpassword");
        user.setStatus(StatusEnum.CONFIRMED);
        user.setClasses(schoolClass);
        user.setCreatedAt(now);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setExternalId("local123");
        user.setRefreshToken("newrefresh");

        Assertions.assertEquals("Test test", user.getFullName());
        Assertions.assertEquals("test@example.com", user.getEmail());
        Assertions.assertEquals("newpassword", user.getPassword());
        Assertions.assertEquals(StatusEnum.CONFIRMED, user.getStatus());
        Assertions.assertEquals(schoolClass, user.getClasses());
        Assertions.assertEquals(now, user.getCreatedAt());
        Assertions.assertEquals(AuthProvider.LOCAL, user.getAuthProvider());
        Assertions.assertEquals("local123", user.getExternalId());
        Assertions.assertEquals("newrefresh", user.getRefreshToken());
    }

    @Test
    void hasCorrectDefaultValues() {
        Users user = new Users();
        user.onCreate();

        Assertions.assertNull(user.getId());
        Assertions.assertNull(user.getFullName());
        Assertions.assertNull(user.getEmail());
        Assertions.assertNull(user.getPassword());
        Assertions.assertEquals(StatusEnum.PENDING, user.getStatus());
        Assertions.assertNull(user.getClasses());
        Assertions.assertNotNull(user.getCreatedAt());
        Assertions.assertNull(user.getAuthProvider());
        Assertions.assertNull(user.getExternalId());
        Assertions.assertNull(user.getRefreshToken());
        Assertions.assertNotNull(user.getUserRoles());
        Assertions.assertTrue(user.getUserRoles().isEmpty());
    }
}
