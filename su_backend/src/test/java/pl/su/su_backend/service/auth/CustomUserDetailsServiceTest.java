package pl.su.su_backend.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.testsupport.Fixtures;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Users testUser;
    private Role testRole;
    private UserRole testUserRole;
    private String testEmail;

    @BeforeEach
    void setUp() {
        testEmail = "test@test.com";
        testRole = Fixtures.role(RoleCode.ADMINISTRATOR);
        testUser = Fixtures.userWithStatus("Test User", testEmail, StatusEnum.CONFIRMED);
        testUser.setPassword("encodedPassword");

        testUserRole = Fixtures.userRole(testUser, testRole);

        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(testUserRole);
        testUser.setUserRoles(userRoles);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Then
        assertNotNull(result);
        assertEquals(testEmail, result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        assertTrue(result.isEnabled());

        Set<String> authorities = result.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertTrue(authorities.contains("ROLE_ADMINISTRATOR"));
        
        verify(usersRepository).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        // Given
        String nonExistentEmail = "nonexistent@test.com";
        when(usersRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, 
            () -> customUserDetailsService.loadUserByUsername(nonExistentEmail));
        
        assertEquals("User not found with email: " + nonExistentEmail, exception.getMessage());
        verify(usersRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void loadUserByUsername_ShouldHandleOAuth2User_WhenPasswordIsNull() {
        // Given
        testUser.setPassword(null); // OAuth2 user
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Then
        assertNotNull(result);
        assertEquals(testEmail, result.getUsername());
        assertEquals("", result.getPassword());
        assertTrue(result.isEnabled());
        verify(usersRepository).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldDisableUser_WhenStatusIsBlocked() {
        // Given
        testUser.setStatus(StatusEnum.BLOCKED);
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Then
        assertNotNull(result);
        assertFalse(result.isEnabled());
        verify(usersRepository).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldEnableUser_WhenStatusIsPending() {
        // Given
        testUser.setStatus(StatusEnum.PENDING);
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Then
        assertNotNull(result);
        assertTrue(result.isEnabled());
        verify(usersRepository).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldEnableUser_WhenStatusIsConfirmed() {
        // Given
        testUser.setStatus(StatusEnum.CONFIRMED);
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Then
        assertNotNull(result);
        assertTrue(result.isEnabled());
        verify(usersRepository).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldDisableUser_WhenStatusIsNull() {
        // Given
        testUser.setStatus(null);
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Then
        assertNotNull(result);
        assertFalse(result.isEnabled());
        verify(usersRepository).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldMapMultipleRoles_WhenUserHasMultipleRoles() {
        // Given
        Role studentRole = Fixtures.role(RoleCode.UCZEN);
        UserRole studentUserRole = Fixtures.userRole(testUser, studentRole);

        Set<UserRole> userRoles = new HashSet<>();
        userRoles.add(testUserRole); // ADMINISTRATOR
        userRoles.add(studentUserRole); // UCZEN
        testUser.setUserRoles(userRoles);

        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Then
        assertNotNull(result);
        Set<String> authorities = result.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        
        assertTrue(authorities.contains("ROLE_ADMINISTRATOR"));
        assertTrue(authorities.contains("ROLE_UCZEN"));
        assertEquals(2, authorities.size());
        
        verify(usersRepository).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldHandleEmptyRoles_WhenUserHasNoRoles() {
        // Given
        testUser.setUserRoles(new HashSet<>());
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Then
        assertNotNull(result);
        assertTrue(result.getAuthorities().isEmpty());
        verify(usersRepository).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldSetCorrectAccountFlags() {
        // Given
        when(usersRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Then
        assertNotNull(result);
        assertTrue(result.isAccountNonExpired());
        assertTrue(result.isAccountNonLocked());
        assertTrue(result.isCredentialsNonExpired());
        verify(usersRepository).findByEmail(testEmail);
    }
}
