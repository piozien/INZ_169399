package pl.su.su_backend.controller.user;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import pl.su.su_backend.service.auth.JwtService;
import pl.su.su_backend.dto.user.UserRequestDto;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.permission.PermissionRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.testsupport.Fixtures;
import pl.su.su_backend.testsupport.OAuth2TestConfig;
import pl.su.su_backend.testsupport.RolePermissionTestHelper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(OAuth2TestConfig.class)
class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;


    @Autowired
    private JwtService jwtService;

    private Role adminRole;
    private Role teacherRole;
    private Role studentRole;
    private Users adminUser;
    private Users teacherUser;
    private Users studentUser;
    private String adminToken;
    private String teacherToken;
    private String studentToken;

    @BeforeEach
    void setUp() {
        flyway.migrate();

        userRoleRepository.deleteAll();
        usersRepository.deleteAll();

        adminRole = RolePermissionTestHelper.ensureRole(
                roleRepository, permissionRepository, RoleCode.DYREKTOR,
                PermissionCode.USER_VIEW,
                PermissionCode.USER_EDIT,
                PermissionCode.USER_DELETE,
                PermissionCode.USER_ASSIGN_ROLE,
                PermissionCode.ROLE_MANAGE
        );

        teacherRole = RolePermissionTestHelper.ensureRole(
                roleRepository, permissionRepository, RoleCode.NAUCZYCIEL,
                PermissionCode.USER_VIEW
        );

        studentRole = RolePermissionTestHelper.ensureRole(
                roleRepository, permissionRepository, RoleCode.UCZEN,
                PermissionCode.USER_VIEW
        );

        adminUser = Fixtures.createUserWithRole(
                usersRepository,
                userRoleRepository,
                "Admin",
                "admin@test.local",
                StatusEnum.CONFIRMED,
                AuthProvider.LOCAL,
                adminRole
        );

        teacherUser = Fixtures.createUserWithRole(
                usersRepository,
                userRoleRepository,
                "Teacher",
                "teacher@test.local",
                StatusEnum.CONFIRMED,
                AuthProvider.LOCAL,
                teacherRole
        );

        studentUser = Fixtures.createUserWithRole(
                usersRepository,
                userRoleRepository,
                "Student",
                "student@test.local",
                StatusEnum.CONFIRMED,
                AuthProvider.LOCAL,
                studentRole
        );

        adminToken = jwtService.generateToken(adminUser.getEmail());
        teacherToken = jwtService.generateToken(teacherUser.getEmail());
        studentToken = jwtService.generateToken(studentUser.getEmail());
    }

    @Test
    void getAllUsers_ShouldReturnOk_WhenAdminHasPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("admin@test.local");
    }

    @Test
    void getAllUsers_ShouldReturnOwnRecord_WhenStudentHasViewPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(studentToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("student@test.local");
        assertThat(response.getBody()).doesNotContain("teacher@test.local");
    }

    @Test
    void getUserById_ShouldReturnOk_WhenAdminHasPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/" + studentUser.getId(),
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("student@test.local");
    }

    @Test
    void getUserByEmail_ShouldReturnOk_WhenAdminHasPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/email/" + studentUser.getEmail(),
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("student@test.local");
    }

    @Test
    void getUserRoles_ShouldReturnOk_WhenAdminHasPermission() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/" + studentUser.getId() + "/roles",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UCZEN");
    }


    @Test
    void getMe_ShouldReturnOk_ForAuthenticatedUser() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/me",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(teacherToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("teacher@test.local");
    }

    @Test
    void updateUser_ShouldReturnOk_WhenAdminHasPermission() {
        UserRequestDto request = Fixtures.userRequestDto(
                "Updated Teacher",
                "teacher.updated@test.local",
                "newPassword123"
        );

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/" + teacherUser.getId(),
                HttpMethod.PUT,
                Fixtures.httpEntityWithToken(adminToken, request),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Users updated = usersRepository.findById(teacherUser.getId()).orElseThrow();
        assertThat(updated.getEmail()).isEqualTo("teacher.updated@test.local");
    }

    @Test
    void deleteUser_ShouldReturnNoContent_WhenAdminHasPermission() {
        Users tempUser = Fixtures.createUserWithRole(
                usersRepository,
                userRoleRepository,
                "Temp",
                "temp-" + UUID.randomUUID() + "@test.local",
                StatusEnum.CONFIRMED,
                AuthProvider.LOCAL,
                studentRole
        );

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/" + tempUser.getId(),
                HttpMethod.DELETE,
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Users blocked = usersRepository.findById(tempUser.getId()).orElseThrow();
        assertThat(blocked.getStatus()).isEqualTo(StatusEnum.BLOCKED);
    }

    @Test
    void blockUser_ShouldReturnOk_WhenAdminHasPermission() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users/" + studentUser.getId() + "/block",
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Users blocked = usersRepository.findById(studentUser.getId()).orElseThrow();
        assertThat(blocked.getStatus()).isEqualTo(StatusEnum.BLOCKED);
    }

    @Test
    void unblockUser_ShouldReturnOk_WhenAdminHasPermission() {
        restTemplate.postForEntity(
                "/api/users/" + studentUser.getId() + "/block",
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users/" + studentUser.getId() + "/unblock",
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Users unblocked = usersRepository.findById(studentUser.getId()).orElseThrow();
        assertThat(unblocked.getStatus()).isEqualTo(StatusEnum.CONFIRMED);
    }

    @Test
    void assignRole_ShouldReturnOk_WhenAdminHasPermission() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users/" + studentUser.getId() + "/roles/" + RoleCode.NAUCZYCIEL,
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Role teacherRoleDb = roleRepository.findByRoleCode(RoleCode.NAUCZYCIEL).orElseThrow();
        boolean hasTeacherRole = userRoleRepository.existsByUser_IdAndRole_Id(studentUser.getId(), teacherRoleDb.getId());
        assertThat(hasTeacherRole).isTrue();
    }

    @Test
    void removeRole_ShouldReturnOk_WhenAdminHasPermission() {
        Role removableRole = RolePermissionTestHelper.ensureRole(
                roleRepository, permissionRepository, RoleCode.BYLY_CZLONEK_SU);

        ResponseEntity<String> assignResponse = restTemplate.postForEntity(
                "/api/users/" + studentUser.getId() + "/roles/" + removableRole.getRoleCode(),
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );

        assertThat(assignResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/users/" + studentUser.getId() + "/roles/" + removableRole.getRoleCode(),
                HttpMethod.DELETE,
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> rolesResponse = restTemplate.exchange(
                "/api/users/" + studentUser.getId() + "/roles",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(adminToken),
                String.class
        );
        assertThat(rolesResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rolesResponse.getBody()).doesNotContain(removableRole.getRoleCode().name());
    }
}


