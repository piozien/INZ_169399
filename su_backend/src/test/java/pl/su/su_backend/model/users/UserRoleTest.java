package pl.su.su_backend.model.users;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.testsupport.Fixtures;

public class UserRoleTest {

    @Test
    void builderSetsAllFields() {
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        Role role = Fixtures.role(RoleCode.ADMINISTRATOR, "Admin role");
        UserRole.Id id = new UserRole.Id(user.getId(), role.getId());

        UserRole userRole = UserRole.builder()
                .id(id)
                .user(user)
                .role(role)
                .build();

        Assertions.assertEquals(id, userRole.getId());
        Assertions.assertEquals(user, userRole.getUser());
        Assertions.assertEquals(role, userRole.getRole());
    }

    @Test
    void onAssignSetCorrectDate() {
        UserRole ur = UserRole.builder()
                .assignedAt(null).build();
        Assertions.assertNull(ur.getAssignedAt());
        ur.onAssign();
        Assertions.assertNotNull(ur.getAssignedAt());
        System.out.println("Value of the assignedAt field is: "
                + ur.getAssignedAt());
    }

    @Test
    void canChangeFieldsViaSetters() {
        UserRole userRole = new UserRole();
        Users user = Fixtures.simpleUser("Test User", "test@example.com");
        Role role = Fixtures.role(RoleCode.NAUCZYCIEL, "Teacher role");
        UserRole.Id id = new UserRole.Id(user.getId(), role.getId());

        userRole.setId(id);
        userRole.setUser(user);
        userRole.setRole(role);

        Assertions.assertEquals(id, userRole.getId());
        Assertions.assertEquals(user, userRole.getUser());
        Assertions.assertEquals(role, userRole.getRole());
    }

    @Test
    void hasCorrectDefaultValues() {
        UserRole userRole = new UserRole();

        Assertions.assertNull(userRole.getId());
        Assertions.assertNull(userRole.getUser());
        Assertions.assertNull(userRole.getRole());
    }

    @Test
    void canHandleNullValues() {
        UserRole userRole = new UserRole();

        userRole.setUser(null);
        userRole.setRole(null);

        Assertions.assertNull(userRole.getUser());
        Assertions.assertNull(userRole.getRole());
    }

    @Test
    void canHandleDifferentRoleTypes() {
        UserRole userRole = new UserRole();

        for (RoleCode roleCode : RoleCode.values()) {
            Role role = Fixtures.role(roleCode, "Role " + roleCode.name());
            userRole.setRole(role);
            Assertions.assertEquals(role, userRole.getRole());
        }
    }
}
