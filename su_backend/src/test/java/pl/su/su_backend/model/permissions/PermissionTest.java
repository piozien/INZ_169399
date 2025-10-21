package pl.su.su_backend.model.permissions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.testsupport.Fixtures;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PermissionTest {

    @Test
    void builderSetsAllFields() {
        UUID id = UUID.randomUUID();
        String name = "USER_CREATE";
        String description = "Create user permission";
        Set<Role> roles = new HashSet<>();
        Role role1 = Fixtures.role(RoleCode.ADMINISTRATOR, "Admin role");
        Role role2 = Fixtures.role(RoleCode.NAUCZYCIEL, "Teacher role");
        roles.add(role1);
        roles.add(role2);

        Permission permission = Permission.builder()
                .id(id)
                .name(name)
                .description(description)
                .roles(roles)
                .build();

        Assertions.assertEquals(id, permission.getId());
        Assertions.assertEquals(name, permission.getName());
        Assertions.assertEquals(description, permission.getDescription());
        Assertions.assertEquals(roles, permission.getRoles());
    }

    @Test
    void canChangeFieldsViaSetters() {
        Permission permission = new Permission();
        UUID id = UUID.randomUUID();
        String name = "USER_EDIT";
        String description = "Edit user permission";
        Set<Role> roles = new HashSet<>();

        permission.setId(id);
        permission.setName(name);
        permission.setDescription(description);
        permission.setRoles(roles);

        Assertions.assertEquals(id, permission.getId());
        Assertions.assertEquals(name, permission.getName());
        Assertions.assertEquals(description, permission.getDescription());
        Assertions.assertEquals(roles, permission.getRoles());
    }

    @Test
    void hasCorrectDefaultValues() {
        Permission permission = new Permission();

        Assertions.assertNull(permission.getId());
        Assertions.assertNull(permission.getName());
        Assertions.assertNull(permission.getDescription());
        Assertions.assertNotNull(permission.getRoles());
        Assertions.assertTrue(permission.getRoles().isEmpty());
    }

    @Test
    void canHandleNullValues() {
        Permission permission = new Permission();

        permission.setName(null);
        permission.setDescription(null);
        permission.setRoles(null);

        Assertions.assertNull(permission.getName());
        Assertions.assertNull(permission.getDescription());
        Assertions.assertNull(permission.getRoles());
    }

    @Test
    void canHandleEmptyStringValues() {
        Permission permission = new Permission();

        permission.setName("");
        permission.setDescription("");

        Assertions.assertEquals("", permission.getName());
        Assertions.assertEquals("", permission.getDescription());
    }

    @Test
    void canHandleSpecialCharactersInName() {
        Permission permission = new Permission();
        String nameWithSpecialChars = "USER_SPECIAL_@#$%^&*()";

        permission.setName(nameWithSpecialChars);

        Assertions.assertEquals(nameWithSpecialChars, permission.getName());
    }

    @Test
    void canHandleLongDescription() {
        Permission permission = new Permission();
        String longDescription = "A".repeat(1000);

        permission.setDescription(longDescription);

        Assertions.assertEquals(longDescription, permission.getDescription());
    }

    @Test
    void canHandleRolesCollection() {
        Permission permission = new Permission();
        Set<Role> roles = new HashSet<>();
        Role role1 = Fixtures.role(RoleCode.ADMINISTRATOR, "Admin role");
        Role role2 = Fixtures.role(RoleCode.NAUCZYCIEL, "Teacher role");
        roles.add(role1);
        roles.add(role2);

        permission.setRoles(roles);

        Assertions.assertEquals(2, permission.getRoles().size());
        Assertions.assertTrue(permission.getRoles().contains(role1));
        Assertions.assertTrue(permission.getRoles().contains(role2));
    }

    @Test
    void canHandleEmptyRolesCollection() {
        Permission permission = new Permission();
        Set<Role> emptyRoles = new HashSet<>();

        permission.setRoles(emptyRoles);

        Assertions.assertTrue(permission.getRoles().isEmpty());
    }
}
