package pl.su.su_backend.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.testsupport.Fixtures;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentServiceTest {

    @Mock private UsersRepository usersRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserRoleRepository userRoleRepository;
    @Mock private ActivityLogService activityLogService;
    @Mock private PermissionService permissionService;

    @InjectMocks
    private RoleAssignmentService roleAssignmentService;

    private Users actingUser;
    private Users targetUser;
    private Role adminRole;
    private Role teacherRole;
    private Role studentRole;

    @BeforeEach
    void setUp() {
        actingUser = Fixtures.user("Admin User", "admin@test.com");
        actingUser.setId(UUID.randomUUID());
        actingUser.setStatus(StatusEnum.CONFIRMED);

        targetUser = Fixtures.user("Target User", "target@test.com");
        targetUser.setId(UUID.randomUUID());
        targetUser.setStatus(StatusEnum.CONFIRMED);

        adminRole = Fixtures.role(RoleCode.ADMINISTRATOR);
        teacherRole = Fixtures.role(RoleCode.NAUCZYCIEL);
        studentRole = Fixtures.role(RoleCode.UCZEN);
    }


    @Test
    void assignRole_ShouldAssignRoleSuccessfully_WhenValidData() {
        // Given
        String reason = "Promotion to teacher";
        
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.NAUCZYCIEL)).thenReturn(Optional.of(teacherRole));
        when(permissionService.hasPermission(actingUser.getId(), PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(userRoleRepository.existsByUser_IdAndRole_Id(targetUser.getId(), teacherRole.getId())).thenReturn(false);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(new UserRole());
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        // When
        roleAssignmentService.assignRole(actingUser.getId(), targetUser.getId(), RoleCode.NAUCZYCIEL, reason);

        // Then
        verify(userRoleRepository).save(any(UserRole.class));
        verify(activityLogService).log(eq(actingUser.getId()), eq(ActionType.ASSIGN_ROLE), anyString());
    }

    @Test
    void assignRole_ShouldThrowException_WhenActingUserNotFound() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(usersRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleAssignmentService.assignRole(nonExistentUserId, targetUser.getId(), RoleCode.NAUCZYCIEL, "reason");
        });

        assertEquals("User not found: " + nonExistentUserId, exception.getMessage());
    }

    @Test
    void assignRole_ShouldThrowException_WhenTargetUserIsBlocked() {
        // Given
        targetUser.setStatus(StatusEnum.BLOCKED);
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.NAUCZYCIEL)).thenReturn(Optional.of(teacherRole));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleAssignmentService.assignRole(actingUser.getId(), targetUser.getId(), RoleCode.NAUCZYCIEL, "reason");
        });

        assertEquals("Cannot modify roles of blocked user", exception.getMessage());
    }

    @Test
    void assignRole_ShouldThrowException_WhenTryingToAssignSURoleGlobally() {
        // Given
        Role suRole = Fixtures.role(RoleCode.PRZEWODNICZACY_SU);
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.PRZEWODNICZACY_SU)).thenReturn(Optional.of(suRole));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleAssignmentService.assignRole(actingUser.getId(), targetUser.getId(), RoleCode.PRZEWODNICZACY_SU, "reason");
        });

        assertEquals("Cannot assign SU roles globally. SU roles must be assigned through council membership.", exception.getMessage());
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void assignRole_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.NAUCZYCIEL)).thenReturn(Optional.of(teacherRole));
        when(permissionService.hasPermission(actingUser.getId(), PermissionCode.USER_ASSIGN_ROLE)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleAssignmentService.assignRole(actingUser.getId(), targetUser.getId(), RoleCode.NAUCZYCIEL, "reason");
        });

        assertEquals("You are not allowed to assign roles", exception.getMessage());
    }

    @Test
    void assignRole_ShouldDoNothing_WhenRoleAlreadyAssigned() {
        // Given
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.NAUCZYCIEL)).thenReturn(Optional.of(teacherRole));
        when(permissionService.hasPermission(actingUser.getId(), PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(userRoleRepository.existsByUser_IdAndRole_Id(targetUser.getId(), teacherRole.getId())).thenReturn(true);

        // When
        roleAssignmentService.assignRole(actingUser.getId(), targetUser.getId(), RoleCode.NAUCZYCIEL, "reason");

        // Then
        verify(userRoleRepository, never()).save(any(UserRole.class));
        verify(activityLogService, never()).log(any(UUID.class), any(), anyString());
    }


    @Test
    void assignRoleByEmail_ShouldAssignRoleSuccessfully_WhenValidEmail() {
        // Given
        String actingEmail = "admin@test.com";
        String reason = "Promotion";
        
        when(usersRepository.findByEmail(actingEmail)).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.NAUCZYCIEL)).thenReturn(Optional.of(teacherRole));
        when(permissionService.hasPermission(actingUser.getId(), PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(userRoleRepository.existsByUser_IdAndRole_Id(targetUser.getId(), teacherRole.getId())).thenReturn(false);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(new UserRole());
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        // When
        roleAssignmentService.assignRoleByEmail(actingEmail, targetUser.getId(), RoleCode.NAUCZYCIEL, reason);

        // Then
        verify(userRoleRepository).save(any(UserRole.class));
        verify(activityLogService).log(eq(actingUser.getId()), eq(ActionType.ASSIGN_ROLE), anyString());
    }

    @Test
    void assignRoleByEmail_ShouldThrowException_WhenActingUserEmailNotFound() {
        // Given
        String nonExistentEmail = "nonexistent@test.com";
        when(usersRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleAssignmentService.assignRoleByEmail(nonExistentEmail, targetUser.getId(), RoleCode.NAUCZYCIEL, "reason");
        });

        assertEquals("Acting user not found: " + nonExistentEmail, exception.getMessage());
    }


    @Test
    void revokeRole_ShouldRevokeRoleSuccessfully_WhenValidData() {
        // Given
        String reason = "Demotion";
        UserRole actingUserRole = Fixtures.userRole(actingUser, adminRole);
        UserRole targetUserRole = Fixtures.userRole(targetUser, studentRole);
        actingUser.setUserRoles(new HashSet<>(Set.of(actingUserRole)));
        targetUser.setUserRoles(new HashSet<>(Set.of(targetUserRole)));
        
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.UCZEN)).thenReturn(Optional.of(studentRole));
        when(permissionService.hasPermission(actingUser.getId(), PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(userRoleRepository.existsByUser_IdAndRole_Id(targetUser.getId(), studentRole.getId())).thenReturn(true);
        when(userRoleRepository.findById(new UserRole.Id(targetUser.getId(), studentRole.getId()))).thenReturn(Optional.of(targetUserRole));
        doNothing().when(userRoleRepository).delete(any(UserRole.class));
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        // When
        roleAssignmentService.revokeRole(actingUser.getId(), targetUser.getId(), RoleCode.UCZEN, reason);

        // Then
        verify(userRoleRepository).delete(targetUserRole);
        assertThat(targetUser.getUserRoles()).doesNotContain(targetUserRole);
        verify(activityLogService).log(eq(actingUser.getId()), eq(ActionType.REMOVE_ROLE), anyString());
    }

    @Test
    void revokeRole_ShouldThrowException_WhenNoPermission() {
        // Given
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.UCZEN)).thenReturn(Optional.of(studentRole));
        when(permissionService.hasPermission(actingUser.getId(), PermissionCode.USER_ASSIGN_ROLE)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleAssignmentService.revokeRole(actingUser.getId(), targetUser.getId(), RoleCode.UCZEN, "reason");
        });

        assertEquals("You are not allowed to revoke roles", exception.getMessage());
    }

    @Test
    void revokeRole_ShouldThrowException_WhenInsufficientRank() {
        // Given
        String reason = "Demotion";
        UserRole actingUserRole = Fixtures.userRole(actingUser, studentRole);
        UserRole targetUserRole = Fixtures.userRole(targetUser, adminRole);
        actingUser.setUserRoles(new HashSet<>(Set.of(actingUserRole)));
        targetUser.setUserRoles(new HashSet<>(Set.of(targetUserRole)));
        
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.ADMINISTRATOR)).thenReturn(Optional.of(adminRole));
        when(permissionService.hasPermission(actingUser.getId(), PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleAssignmentService.revokeRole(actingUser.getId(), targetUser.getId(), RoleCode.ADMINISTRATOR, reason);
        });

        assertEquals("You cannot revoke roles from users with higher or equal rank than yours", exception.getMessage());
    }

    @Test
    void revokeRole_ShouldThrowException_WhenRevokingLastAdministrator() {
        // Given
        String reason = "Demotion";
        UserRole actingUserRole = Fixtures.userRole(actingUser, adminRole);
        UserRole targetUserRole = Fixtures.userRole(targetUser, adminRole);
        actingUser.setUserRoles(new HashSet<>(Set.of(actingUserRole)));
        targetUser.setUserRoles(new HashSet<>(Set.of(targetUserRole)));
        
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.ADMINISTRATOR)).thenReturn(Optional.of(adminRole));
        when(permissionService.hasPermission(actingUser.getId(), PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(userRoleRepository.findByRole_Id(adminRole.getId())).thenReturn(List.of(targetUserRole));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleAssignmentService.revokeRole(actingUser.getId(), targetUser.getId(), RoleCode.ADMINISTRATOR, reason);
        });

        assertEquals("Cannot revoke the last ADMINISTRATOR role", exception.getMessage());
    }

    @Test
    void revokeRole_ShouldDoNothing_WhenUserDoesNotHaveRole() {
        // Given
        String reason = "Demotion";
        UserRole actingUserRole = Fixtures.userRole(actingUser, adminRole);
        actingUser.setUserRoles(new HashSet<>(Set.of(actingUserRole)));
        targetUser.setUserRoles(new HashSet<>());
        
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.UCZEN)).thenReturn(Optional.of(studentRole));
        when(permissionService.hasPermission(actingUser.getId(), PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(userRoleRepository.existsByUser_IdAndRole_Id(targetUser.getId(), studentRole.getId())).thenReturn(false);

        // When
        roleAssignmentService.revokeRole(actingUser.getId(), targetUser.getId(), RoleCode.UCZEN, reason);

        // Then
        verify(userRoleRepository, never()).delete(any(UserRole.class));
        verify(activityLogService, never()).log(any(UUID.class), any(), anyString());
    }


    @Test
    void revokeRoleByEmail_ShouldRevokeRoleSuccessfully_WhenValidEmail() {
        // Given
        String actingEmail = "admin@test.com";
        String reason = "Demotion";
        UserRole actingUserRole = Fixtures.userRole(actingUser, adminRole);
        UserRole targetUserRole = Fixtures.userRole(targetUser, studentRole);
        actingUser.setUserRoles(new HashSet<>(Set.of(actingUserRole)));
        targetUser.setUserRoles(new HashSet<>(Set.of(targetUserRole)));
        
        when(usersRepository.findByEmail(actingEmail)).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(actingUser.getId())).thenReturn(Optional.of(actingUser));
        when(usersRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(roleRepository.findByRoleCode(RoleCode.UCZEN)).thenReturn(Optional.of(studentRole));
        when(permissionService.hasPermission(actingUser.getId(), PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(userRoleRepository.existsByUser_IdAndRole_Id(targetUser.getId(), studentRole.getId())).thenReturn(true);
        when(userRoleRepository.findById(new UserRole.Id(targetUser.getId(), studentRole.getId()))).thenReturn(Optional.of(targetUserRole));
        doNothing().when(userRoleRepository).delete(any(UserRole.class));
        doNothing().when(activityLogService).log(any(UUID.class), any(), anyString());

        // When
        roleAssignmentService.revokeRoleByEmail(actingEmail, targetUser.getId(), RoleCode.UCZEN, reason);

        // Then
        verify(userRoleRepository).delete(targetUserRole);
        assertThat(targetUser.getUserRoles()).doesNotContain(targetUserRole);
        verify(activityLogService).log(eq(actingUser.getId()), eq(ActionType.REMOVE_ROLE), anyString());
    }

    @Test
    void revokeRoleByEmail_ShouldThrowException_WhenActingUserEmailNotFound() {
        // Given
        String nonExistentEmail = "nonexistent@test.com";
        when(usersRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleAssignmentService.revokeRoleByEmail(nonExistentEmail, targetUser.getId(), RoleCode.UCZEN, "reason");
        });

        assertEquals("Acting user not found: " + nonExistentEmail, exception.getMessage());
    }
}