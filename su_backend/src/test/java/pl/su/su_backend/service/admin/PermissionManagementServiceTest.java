package pl.su.su_backend.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.permissions.Permission;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.repositories.permission.PermissionRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.testsupport.Fixtures;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionManagementServiceTest {

    @Mock
    private PermissionRepository permissionRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private PermissionService permissionService;
    
    @InjectMocks
    private PermissionManagementService permissionManagementService;
    
    private String testUserEmail;
    private Role testRole;
    private Permission testPermission;
    
    @BeforeEach
    void setUp() {
        testUserEmail = "admin@test.com";
        
        testRole = Fixtures.role(RoleCode.ADMINISTRATOR, "Admin Role");
        testRole.setId(UUID.randomUUID());
        
        testPermission = Fixtures.permission(PermissionCode.USER_ASSIGN_ROLE.getCode(), "user.assign_role");
        testPermission.setId(UUID.randomUUID());
    }

    
    @Test
    void getRolePermissions_ShouldReturnPermissions_WhenValidRoleAndHasPermission() {
        // Given
        Set<Permission> permissions = new HashSet<>();
        permissions.add(testPermission);
        testRole.setPermissions(permissions);
        
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(roleRepository.findByRoleCode(RoleCode.ADMINISTRATOR)).thenReturn(Optional.of(testRole));

        // When
        List<String> result = permissionManagementService.getRolePermissions(RoleCode.ADMINISTRATOR, testUserEmail);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user.assign_role", result.getFirst());
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
        verify(roleRepository).findByRoleCode(RoleCode.ADMINISTRATOR);
    }
    
    @Test
    void getRolePermissions_ShouldThrowException_WhenNoPermission() {
        // Given
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            permissionManagementService.getRolePermissions(RoleCode.ADMINISTRATOR, testUserEmail));
        
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
        verifyNoInteractions(roleRepository);
    }
    
    @Test
    void getRolePermissions_ShouldThrowException_WhenRoleNotFound() {
        // Given
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(roleRepository.findByRoleCode(RoleCode.ADMINISTRATOR)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            permissionManagementService.getRolePermissions(RoleCode.ADMINISTRATOR, testUserEmail));
        
        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getCode());
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
        verify(roleRepository).findByRoleCode(RoleCode.ADMINISTRATOR);
    }

    
    @Test
    void getAllPermissions_ShouldReturnAllPermissions_WhenHasPermission() {
        // Given
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);

        // When
        List<PermissionCode> result = permissionManagementService.getAllPermissions(testUserEmail);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(PermissionCode.USER_ASSIGN_ROLE));
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
    }
    
    @Test
    void getAllPermissions_ShouldThrowException_WhenNoPermission() {
        // Given
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            permissionManagementService.getAllPermissions(testUserEmail));
        
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
    }

    
    @Test
    void getAllRoles_ShouldReturnAllRoles_WhenHasPermission() {
        // Given
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);

        // When
        List<RoleCode> result = permissionManagementService.getAllRoles(testUserEmail);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains(RoleCode.ADMINISTRATOR));
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
    }
    
    @Test
    void getAllRoles_ShouldThrowException_WhenNoPermission() {
        // Given
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            permissionManagementService.getAllRoles(testUserEmail));
        
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
    }

    
    @Test
    void assignPermissionToRole_ShouldAssignPermission_WhenValidData() {
        // Given
        testRole.setPermissions(new HashSet<>());
        
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(roleRepository.findByRoleCode(RoleCode.ADMINISTRATOR)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByName(PermissionCode.USER_ASSIGN_ROLE.getCode())).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        permissionManagementService.assignPermissionToRole(RoleCode.ADMINISTRATOR, PermissionCode.USER_ASSIGN_ROLE, testUserEmail);

        // Then
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
        verify(roleRepository).findByRoleCode(RoleCode.ADMINISTRATOR);
        verify(permissionRepository).findByName(PermissionCode.USER_ASSIGN_ROLE.getCode());
        verify(roleRepository).save(testRole);
        assertTrue(testRole.getPermissions().contains(testPermission));
    }
    
    @Test
    void assignPermissionToRole_ShouldNotAssign_WhenPermissionAlreadyExists() {
        // Given
        Set<Permission> permissions = new HashSet<>();
        permissions.add(testPermission);
        testRole.setPermissions(permissions);
        
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(roleRepository.findByRoleCode(RoleCode.ADMINISTRATOR)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByName(PermissionCode.USER_ASSIGN_ROLE.getCode())).thenReturn(Optional.of(testPermission));

        // When
        permissionManagementService.assignPermissionToRole(RoleCode.ADMINISTRATOR, PermissionCode.USER_ASSIGN_ROLE, testUserEmail);

        // Then
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
        verify(roleRepository).findByRoleCode(RoleCode.ADMINISTRATOR);
        verify(permissionRepository).findByName(PermissionCode.USER_ASSIGN_ROLE.getCode());
        verify(roleRepository, never()).save(any(Role.class));
    }
    
    @Test
    void assignPermissionToRole_ShouldThrowException_WhenNoPermission() {
        // Given
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            permissionManagementService.assignPermissionToRole(RoleCode.ADMINISTRATOR, PermissionCode.USER_ASSIGN_ROLE, testUserEmail));
        
        assertTrue(exception.getMessage().contains("Access denied"), exception.getMessage());
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
        verifyNoInteractions(roleRepository);
        verifyNoInteractions(permissionRepository);
    }
    
    @Test
    void assignPermissionToRole_ShouldThrowException_WhenRoleNotFound() {
        // Given
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(roleRepository.findByRoleCode(RoleCode.ADMINISTRATOR)).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            permissionManagementService.assignPermissionToRole(RoleCode.ADMINISTRATOR, PermissionCode.USER_ASSIGN_ROLE, testUserEmail));
        
        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getCode());
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
        verify(roleRepository).findByRoleCode(RoleCode.ADMINISTRATOR);
        verifyNoInteractions(permissionRepository);
    }
    
    @Test
    void assignPermissionToRole_ShouldThrowException_WhenPermissionNotFound() {
        // Given
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(roleRepository.findByRoleCode(RoleCode.ADMINISTRATOR)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByName(PermissionCode.USER_ASSIGN_ROLE.getCode())).thenReturn(Optional.empty());

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            permissionManagementService.assignPermissionToRole(RoleCode.ADMINISTRATOR, PermissionCode.USER_ASSIGN_ROLE, testUserEmail));
        
        assertEquals(ErrorCode.VALIDATION_ERROR, exception.getCode());
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
        verify(roleRepository).findByRoleCode(RoleCode.ADMINISTRATOR);
        verify(permissionRepository).findByName(PermissionCode.USER_ASSIGN_ROLE.getCode());
    }

    
    @Test
    void revokePermissionFromRole_ShouldRevokePermission_WhenValidData() {
        // Given
        Set<Permission> permissions = new HashSet<>();
        permissions.add(testPermission);
        testRole.setPermissions(permissions);
        
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(roleRepository.findByRoleCode(RoleCode.ADMINISTRATOR)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByName(PermissionCode.USER_ASSIGN_ROLE.getCode())).thenReturn(Optional.of(testPermission));
        when(roleRepository.save(any(Role.class))).thenReturn(testRole);

        // When
        permissionManagementService.revokePermissionFromRole(RoleCode.ADMINISTRATOR, PermissionCode.USER_ASSIGN_ROLE, testUserEmail);

        // Then
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
        verify(roleRepository).findByRoleCode(RoleCode.ADMINISTRATOR);
        verify(permissionRepository).findByName(PermissionCode.USER_ASSIGN_ROLE.getCode());
        verify(roleRepository).save(testRole);
        assertFalse(testRole.getPermissions().contains(testPermission));
    }
    
    @Test
    void revokePermissionFromRole_ShouldNotRevoke_WhenPermissionNotAssigned() {
        // Given
        testRole.setPermissions(new HashSet<>());
        
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(roleRepository.findByRoleCode(RoleCode.ADMINISTRATOR)).thenReturn(Optional.of(testRole));
        when(permissionRepository.findByName(PermissionCode.USER_ASSIGN_ROLE.getCode())).thenReturn(Optional.of(testPermission));

        // When
        permissionManagementService.revokePermissionFromRole(RoleCode.ADMINISTRATOR, PermissionCode.USER_ASSIGN_ROLE, testUserEmail);

        // Then
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
        verify(roleRepository).findByRoleCode(RoleCode.ADMINISTRATOR);
        verify(permissionRepository).findByName(PermissionCode.USER_ASSIGN_ROLE.getCode());
        verify(roleRepository, never()).save(any(Role.class));
    }
    
    @Test
    void revokePermissionFromRole_ShouldThrowException_WhenNoPermission() {
        // Given
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            permissionManagementService.revokePermissionFromRole(RoleCode.ADMINISTRATOR, PermissionCode.USER_ASSIGN_ROLE, testUserEmail));
        
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
        verifyNoInteractions(roleRepository);
        verifyNoInteractions(permissionRepository);
    }

    
    @Test
    void getPermissionMatrix_ShouldReturnMatrix_WhenHasPermission() {
        // Given
        Set<Permission> permissions = new HashSet<>();
        permissions.add(testPermission);
        testRole.setPermissions(permissions);
        
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(true);
        when(roleRepository.findByRoleCode(any(RoleCode.class))).thenReturn(Optional.of(testRole));

        // When
        Map<RoleCode, List<String>> result = permissionManagementService.getPermissionMatrix(testUserEmail);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey(RoleCode.ADMINISTRATOR));
        assertEquals(1, result.get(RoleCode.ADMINISTRATOR).size());
        assertEquals("user.assign_role", result.get(RoleCode.ADMINISTRATOR).getFirst());
        verify(permissionService, atLeast(1)).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
    }
    
    @Test
    void getPermissionMatrix_ShouldThrowException_WhenNoPermission() {
        // Given
        when(permissionService.hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE)).thenReturn(false);

        // When & Then
        ApiException exception = assertThrows(ApiException.class, () -> 
            permissionManagementService.getPermissionMatrix(testUserEmail));
        
        assertEquals(ErrorCode.ACCESS_DENIED, exception.getCode());
        verify(permissionService).hasPermission(testUserEmail, PermissionCode.USER_ASSIGN_ROLE);
    }
}
