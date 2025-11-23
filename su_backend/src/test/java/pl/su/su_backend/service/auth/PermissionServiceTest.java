package pl.su.su_backend.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.permissions.Permission;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilMemberRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.testsupport.Fixtures;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static pl.su.su_backend.model.enums.StatusEnum.CONFIRMED;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private UsersRepository usersRepository;
    
    @Mock
    private CouncilMemberRepository councilMemberRepository;
    
    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private PermissionService permissionService;

    private Users testUser;
    private Role testRole;
    private Permission testPermission;
    private String testEmail = "test@test.com";

    @BeforeEach
    void setUp() {
        testUser = Fixtures.userWithStatus("Test User", testEmail, CONFIRMED);
        testRole = Fixtures.role(RoleCode.ADMINISTRATOR);
        testPermission = Fixtures.permission("user.create", "Create users");

        UserRole userRole = Fixtures.userRole(testUser, testRole);
        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(userRole);
        testUser.setUserRoles(userRoles);

        Set<Permission> permissions = new HashSet<>();
        permissions.add(testPermission);
        permissions.add(Fixtures.permission("class_budget.view", "View class budgets"));
        testRole.setPermissions(permissions);
    }

    @Test
    void hasPermission_ShouldReturnTrue_WhenUserHasGlobalPermission() {
        // Given
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(councilMemberRepository.findByIdUserId(testUser.getId())).thenReturn(List.of());

        // When
        boolean result = permissionService.hasPermission(testUser.getId(), PermissionCode.USER_CREATE);

        // Then
        assertTrue(result);
        verify(usersRepository).findById(testUser.getId());
        verify(councilMemberRepository).findByIdUserId(testUser.getId());
    }

    @Test
    void hasPermission_ShouldReturnTrue_WhenUserHasCouncilPermission() {
        // Given
        testUser.setUserRoles(new HashSet<>());

        CouncilMember councilMember = new CouncilMember();
        councilMember.setRole(RoleCode.PRZEWODNICZACY_SU);
        councilMember.setUser(testUser);
        
        Role suRole = Fixtures.role(RoleCode.PRZEWODNICZACY_SU);
        Set<Permission> suPermissions = new HashSet<>();
        suPermissions.add(Fixtures.permission("council.view", "View council"));
        suPermissions.add(Fixtures.permission("council.edit", "Edit council"));
        suRole.setPermissions(suPermissions);
        
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(councilMemberRepository.findByIdUserId(testUser.getId())).thenReturn(List.of(councilMember));
        when(roleRepository.findByRoleCode(RoleCode.PRZEWODNICZACY_SU)).thenReturn(Optional.of(suRole));

        // When
        boolean result = permissionService.hasPermission(testUser.getId(), PermissionCode.COUNCIL_VIEW);

        // Then
        assertTrue(result);
        verify(usersRepository).findById(testUser.getId());
        verify(councilMemberRepository).findByIdUserId(testUser.getId());
        verify(roleRepository).findByRoleCode(RoleCode.PRZEWODNICZACY_SU);
    }

    @Test
    void hasPermission_ShouldReturnTrue_WhenUserHasPermissionFromBothSources() {
        // Given
        Role studentRole = Fixtures.role(RoleCode.UCZEN);
        Set<Permission> studentPermissions = new HashSet<>();
        studentPermissions.add(Fixtures.permission("event.view", "View events"));
        studentRole.setPermissions(studentPermissions);
        
        UserRole userRole = Fixtures.userRole(testUser, studentRole);
        testUser.setUserRoles(Set.of(userRole));
        
        // I dodatkowo ma rolę w samorządzie
        CouncilMember councilMember = new CouncilMember();
        councilMember.setRole(RoleCode.SKARBNIK_SU);
        councilMember.setUser(testUser);
        
        Role suRole = Fixtures.role(RoleCode.SKARBNIK_SU);
        Set<Permission> suPermissions = new HashSet<>();
        suPermissions.add(Fixtures.permission("council_budget.view", "View council budget"));
        suRole.setPermissions(suPermissions);
        
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(councilMemberRepository.findByIdUserId(testUser.getId())).thenReturn(List.of(councilMember));
        when(roleRepository.findByRoleCode(RoleCode.SKARBNIK_SU)).thenReturn(Optional.of(suRole));

        // When
        boolean eventResult = permissionService.hasPermission(testUser.getId(), PermissionCode.EVENT_VIEW);
        boolean budgetResult = permissionService.hasPermission(testUser.getId(), PermissionCode.COUNCIL_BUDGET_VIEW);

        // Then
        assertTrue(eventResult, "Should have EVENT_VIEW from global UCZEN role");
        assertTrue(budgetResult, "Should have COUNCIL_BUDGET_VIEW from council SKARBNIK_SU role");
    }

    @Test
    void hasPermission_ShouldReturnFalse_WhenUserDoesNotHavePermission() {
        // Given
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(councilMemberRepository.findByIdUserId(testUser.getId())).thenReturn(List.of());

        // When
        boolean result = permissionService.hasPermission(testUser.getId(), PermissionCode.USER_DELETE);

        // Then
        assertFalse(result);
        verify(usersRepository).findById(testUser.getId());
    }

    @Test
    void hasPermission_ShouldThrowException_WhenUserNotFound() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        when(usersRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> permissionService.hasPermission(nonExistentUserId, PermissionCode.USER_CREATE));
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(usersRepository).findById(nonExistentUserId);
    }

    @Test
    void hasPermission_ShouldWork_WhenUsingEmail() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(councilMemberRepository.findByIdUserId(testUser.getId())).thenReturn(List.of());

        // When
        boolean result = permissionService.hasPermission(testEmail, PermissionCode.USER_CREATE);

        // Then
        assertTrue(result);
        verify(usersRepository).findByEmail(testEmail);
        verify(usersRepository).findById(testUser.getId());
    }

    @Test
    void hasPermission_ShouldThrowException_WhenEmailNotFound() {
        // Given
        String nonExistentEmail = "nonexistent@test.com";
        when(usersRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, 
            () -> permissionService.hasPermission(nonExistentEmail, PermissionCode.USER_CREATE));
        
        assertTrue(exception.getMessage().contains("User not found"));
        verify(usersRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void canAccessClassBudget_ShouldReturnTrue_WhenUserHasPermission() {
        // Given
        UUID classId = UUID.randomUUID();
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(councilMemberRepository.findByIdUserId(testUser.getId())).thenReturn(List.of());

        // When
        boolean result = permissionService.canAccessClassBudget(testUser.getId(), classId, PermissionCode.CLASS_BUDGET_VIEW);

        // Then
        assertTrue(result);
        verify(usersRepository, times(2)).findById(testUser.getId());
    }

    @Test
    void canAccessClassBudget_ShouldReturnFalse_WhenUserDoesNotHavePermission() {
        // Given
        UUID classId = UUID.randomUUID();
        when(usersRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(councilMemberRepository.findByIdUserId(testUser.getId())).thenReturn(List.of());

        // When
        boolean result = permissionService.canAccessClassBudget(testUser.getId(), classId, PermissionCode.CLASS_BUDGET_DELETE);

        // Then
        assertFalse(result);
        verify(usersRepository, times(2)).findById(testUser.getId());
    }
}
