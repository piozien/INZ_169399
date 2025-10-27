package pl.su.su_backend.model.log;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.testsupport.Fixtures;

import java.time.LocalDateTime;
import java.util.UUID;

public class ActivityLogTest {

    @Test
    void builderSetsAllFields() {
        UUID id = UUID.randomUUID();
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        ActionType actionType = ActionType.REGISTER;
        String action = "User created";
        LocalDateTime createdAt = LocalDateTime.now();

        ActivityLog activityLog = ActivityLog.builder()
                .id(id)
                .user(user)
                .actionType(actionType)
                .action(action)
                .createdAt(createdAt)
                .build();

        Assertions.assertEquals(id, activityLog.getId());
        Assertions.assertEquals(user, activityLog.getUser());
        Assertions.assertEquals(actionType, activityLog.getActionType());
        Assertions.assertEquals(action, activityLog.getAction());
        Assertions.assertEquals(createdAt, activityLog.getCreatedAt());
    }

    @Test
    void canChangeFieldsViaSetters() {
        ActivityLog activityLog = new ActivityLog();
        UUID id = UUID.randomUUID();
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        ActionType actionType = ActionType.DELETE;
        String action = "User deleted";
        LocalDateTime createdAt = LocalDateTime.now();

        activityLog.setId(id);
        activityLog.setUser(user);
        activityLog.setActionType(actionType);
        activityLog.setAction(action);
        activityLog.setCreatedAt(createdAt);

        Assertions.assertEquals(id, activityLog.getId());
        Assertions.assertEquals(user, activityLog.getUser());
        Assertions.assertEquals(actionType, activityLog.getActionType());
        Assertions.assertEquals(action, activityLog.getAction());
        Assertions.assertEquals(createdAt, activityLog.getCreatedAt());
    }

    @Test
    void hasCorrectDefaultValues() {
        ActivityLog activityLog = new ActivityLog();

        Assertions.assertNull(activityLog.getId());
        Assertions.assertNull(activityLog.getUser());
        Assertions.assertNull(activityLog.getActionType());
        Assertions.assertNull(activityLog.getAction());
        Assertions.assertNull(activityLog.getCreatedAt());
    }

    @Test
    void onCreateSetsCreatedAtWhenNull() {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setCreatedAt(null);

        Assertions.assertNull(activityLog.getCreatedAt());
        activityLog.onCreate();
        Assertions.assertNotNull(activityLog.getCreatedAt());
    }

    @Test
    void onCreateDoesNotOverrideExistingCreatedAt() {
        LocalDateTime fixed = LocalDateTime.of(2025, 10, 17, 10, 0);
        ActivityLog activityLog = new ActivityLog();
        activityLog.setCreatedAt(fixed);

        activityLog.onCreate();
        Assertions.assertEquals(fixed, activityLog.getCreatedAt());
    }

    @Test
    void canHandleNullValues() {
        ActivityLog activityLog = new ActivityLog();

        activityLog.setUser(null);
        activityLog.setActionType(null);
        activityLog.setAction(null);
        activityLog.setCreatedAt(null);

        Assertions.assertNull(activityLog.getUser());
        Assertions.assertNull(activityLog.getActionType());
        Assertions.assertNull(activityLog.getAction());
        Assertions.assertNull(activityLog.getCreatedAt());
    }

    @Test
    void canHandleEmptyStringAction() {
        ActivityLog activityLog = new ActivityLog();

        activityLog.setAction("");

        Assertions.assertEquals("", activityLog.getAction());
    }

    @Test
    void canHandleLongActionText() {
        ActivityLog activityLog = new ActivityLog();
        String longAction = "A".repeat(1000);

        activityLog.setAction(longAction);

        Assertions.assertEquals(longAction, activityLog.getAction());
    }

    @Test
    void canHandleSpecialCharactersInAction() {
        ActivityLog activityLog = new ActivityLog();
        String actionWithSpecialChars = "Action with special chars @#$%^&*()";

        activityLog.setAction(actionWithSpecialChars);

        Assertions.assertEquals(actionWithSpecialChars, activityLog.getAction());
    }

    @Test
    void canHandleAllActionTypes() {
        for (ActionType actionType : ActionType.values()) {
            ActivityLog activityLog = new ActivityLog();
            activityLog.setActionType(actionType);
            
            Assertions.assertEquals(actionType, activityLog.getActionType());
        }
    }

    @Test
    void canHandleCreatedAtInThePast() {
        ActivityLog activityLog = new ActivityLog();
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);

        activityLog.setCreatedAt(pastTime);

        Assertions.assertEquals(pastTime, activityLog.getCreatedAt());
    }

    @Test
    void canHandleCreatedAtInTheFuture() {
        ActivityLog activityLog = new ActivityLog();
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);

        activityLog.setCreatedAt(futureTime);

        Assertions.assertEquals(futureTime, activityLog.getCreatedAt());
    }
}
