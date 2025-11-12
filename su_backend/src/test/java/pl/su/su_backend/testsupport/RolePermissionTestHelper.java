package pl.su.su_backend.testsupport;

import lombok.experimental.UtilityClass;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.permissions.Permission;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.repositories.permission.PermissionRepository;
import pl.su.su_backend.repositories.role.RoleRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class RolePermissionTestHelper {

    public Role ensureRole(RoleRepository roleRepository,
                           PermissionRepository permissionRepository,
                           RoleCode roleCode,
                           PermissionCode... permissionCodes) {

        Role role = roleRepository.findByRoleCode(roleCode)
                .orElseGet(() -> roleRepository.save(Fixtures.roleNoId(roleCode)));

        Set<Permission> required = Arrays.stream(permissionCodes)
                .map(code -> permissionRepository.findByName(code.getCode())
                        .orElseGet(() -> permissionRepository.save(Fixtures.permissionNoId(code.getCode(), code.getDescription()))))
                .collect(Collectors.toSet());

        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }

        if (!role.getPermissions().containsAll(required)) {
            role.getPermissions().addAll(required);
        }

        return roleRepository.save(role);
    }
}

