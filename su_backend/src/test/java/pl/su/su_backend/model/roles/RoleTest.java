package pl.su.su_backend.model.roles;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.enums.RoleCategory;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.permissions.Permission;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.testsupport.Fixtures;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RoleTest {

    @Test
    void builderSetsAllFields() {
        UUID id = UUID.randomUUID();
        RoleCode roleCode = RoleCode.ADMINISTRATOR;
        String description = "Administrator role";
        Set<UserRole> userRoles = new HashSet<>();
        Set<Permission> permissions = new HashSet<>();

        Role role = Role.builder()
                .id(id)
                .roleCode(roleCode)
                .description(description)
                .userRoles(userRoles)
                .permissions(permissions)
                .build();

        Assertions.assertEquals(id, role.getId());
        Assertions.assertEquals(roleCode, role.getRoleCode());
        Assertions.assertEquals(description, role.getDescription());
        Assertions.assertEquals(userRoles, role.getUserRoles());
        Assertions.assertEquals(permissions, role.getPermissions());
    }

    @Test
    void canChangeFieldsViaSetters() {
        Role role = new Role();
        UUID id = UUID.randomUUID();
        RoleCode roleCode = RoleCode.UCZEN;
        String description = "Teacher role";
        Set<UserRole> userRoles = new HashSet<>();
        Set<Permission> permissions = new HashSet<>();

        role.setId(id);
        role.setRoleCode(roleCode);
        role.setDescription(description);
        role.setUserRoles(userRoles);
        role.setPermissions(permissions);

        Assertions.assertEquals(id, role.getId());
        Assertions.assertEquals(roleCode, role.getRoleCode());
        Assertions.assertEquals(description, role.getDescription());
        Assertions.assertEquals(userRoles, role.getUserRoles());
        Assertions.assertEquals(permissions, role.getPermissions());
    }

    @Test
    void hasCorrectDefaultValues() {
        Role role = new Role();

        Assertions.assertNull(role.getId());
        Assertions.assertNull(role.getRoleCode());
        Assertions.assertNull(role.getDescription());
        Assertions.assertNotNull(role.getUserRoles());
        Assertions.assertTrue(role.getUserRoles().isEmpty());
        Assertions.assertNotNull(role.getPermissions());
        Assertions.assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    void getCategoryReturnsCorrectCategoryFromRoleCode() {
        Role role = new Role();
        role.setRoleCode(RoleCode.ADMINISTRATOR);

        RoleCategory category = role.getCategory();

        Assertions.assertEquals(RoleCode.ADMINISTRATOR.getCategory(), category);
    }

    @Test
    void getCategoryReturnsNullWhenRoleCodeIsNull() {
        Role role = new Role();
        role.setRoleCode(null);

        RoleCategory category = role.getCategory();

        Assertions.assertNull(category);
    }

    @Test
    void canHandleNullValues() {
        Role role = new Role();

        role.setRoleCode(null);
        role.setDescription(null);
        role.setUserRoles(null);
        role.setPermissions(null);

        Assertions.assertNull(role.getRoleCode());
        Assertions.assertNull(role.getDescription());
        Assertions.assertNull(role.getUserRoles());
        Assertions.assertNull(role.getPermissions());
    }

    @Test
    void canHandleEmptyStringDescription() {
        Role role = new Role();

        role.setDescription("");

        Assertions.assertEquals("", role.getDescription());
    }

    @Test
    void canHandleSpecialCharactersInDescription() {
        Role role = new Role();
        String descriptionWithSpecialChars = "Role with special chars @#$%^&*()";

        role.setDescription(descriptionWithSpecialChars);

        Assertions.assertEquals(descriptionWithSpecialChars, role.getDescription());
    }

    @Test
    void canHandleUserRolesCollection() {
        Role role = new Role();
        Set<UserRole> userRoles = new HashSet<>();
        Users user1 = Fixtures.simpleUser("User 1", "user1@example.com");
        Users user2 = Fixtures.simpleUser("User 2", "user2@example.com");
        Role adminRole = Fixtures.role(RoleCode.ADMINISTRATOR, "Admin role");
        UserRole userRole1 = Fixtures.userRole(user1, adminRole);
        UserRole userRole2 = Fixtures.userRole(user2, adminRole);
        userRoles.add(userRole1);
        userRoles.add(userRole2);

        role.setUserRoles(userRoles);

        Assertions.assertEquals(2, role.getUserRoles().size());
        Assertions.assertTrue(role.getUserRoles().contains(userRole1));
        Assertions.assertTrue(role.getUserRoles().contains(userRole2));
    }

    @Test
    void canHandlePermissionsCollection() {
        Role role = new Role();
        Set<Permission> permissions = new HashSet<>();
        Permission permission1 = Fixtures.permission("USER_CREATE", "Create user permission");
        Permission permission2 = Fixtures.permission("USER_EDIT", "Edit user permission");
        permissions.add(permission1);
        permissions.add(permission2);

        role.setPermissions(permissions);

        Assertions.assertEquals(2, role.getPermissions().size());
        Assertions.assertTrue(role.getPermissions().contains(permission1));
        Assertions.assertTrue(role.getPermissions().contains(permission2));
    }

    @Test
    void canHandleEmptyCollections() {
        Role role = new Role();
        Set<UserRole> emptyUserRoles = new HashSet<>();
        Set<Permission> emptyPermissions = new HashSet<>();

        role.setUserRoles(emptyUserRoles);
        role.setPermissions(emptyPermissions);

        Assertions.assertTrue(role.getUserRoles().isEmpty());
        Assertions.assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    void getCategoryWorksForAllRoleCodes() {
        for (RoleCode roleCode : RoleCode.values()) {
            Role role = new Role();
            role.setRoleCode(roleCode);
            
            Assertions.assertEquals(roleCode.getCategory(), role.getCategory());
        }
    }

    @Test
    void hasHigherOrEqualRank() {
        for (RoleCode a : RoleCode.values()) {
            for (RoleCode b : RoleCode.values()) {
                boolean expected = a.getRank() >= b.getRank();
                Assertions.assertEquals(
                        expected,
                        a.hasHigherOrEqualRankThan(b),
                        () -> "Comparison error between " + a + " and " + b
                );
            }
        }
    }
}
