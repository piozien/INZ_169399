package pl.su.su_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.BaseIntegrationTest;
import pl.su.su_backend.dto.user.ChangePasswordRequestDto;
import pl.su.su_backend.dto.user.UserRequestDto;
import pl.su.su_backend.dto.user.UserUpdateRequestDto;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.JwtService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    EntityManager entityManager;

    private final String ADMIN_EMAIL = "admin@school.edu";
    private final String STUDENT_EMAIL = "student1@school.edu";
    private final String TARGET_EMAIL = "student6@school.edu";


    private final String MIGRATION_PASSWORD = "password123";


    @Test
    void shouldGetOwnProfileSuccessfully() throws Exception {
        Cookie authCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");

        mockMvc.perform(get("/api/users/me")
                        .cookie(authCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(STUDENT_EMAIL));
    }

    @Test
    void shouldForbidGettingOtherUserProfileWithoutPermission() throws Exception {
        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        UUID targetUserId = getUserIdByEmail(TARGET_EMAIL);

        mockMvc.perform(get("/api/users/{id}", targetUserId)
                        .cookie(studentCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToGetOtherUserProfile() throws Exception {
        Cookie adminCookie = generateAuthCookie(ADMIN_EMAIL, "Administrator");
        UUID targetUserId = getUserIdByEmail(STUDENT_EMAIL);

        mockMvc.perform(get("/api/users/{id}", targetUserId)
                        .cookie(adminCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(STUDENT_EMAIL));
    }

    @Test
    void shouldReturn404ForNonExistentUser() throws Exception {
        Cookie adminCookie = generateAuthCookie(ADMIN_EMAIL, "Admin");
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/api/users/{id}", randomId)
                        .cookie(adminCookie))
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldChangePasswordSuccessfully() throws Exception {
        Cookie authCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        String newPassword = "newPassword123";
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                MIGRATION_PASSWORD,
                newPassword
        );

        mockMvc.perform(patch("/api/users/change-password")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        Users updatedUser = usersRepository.findByEmail(STUDENT_EMAIL).get();

        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();

    }

    @Test
    void shouldRejectPasswordChangeIfOldPasswordIsWrong() throws Exception {
        Cookie authCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");

        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                "lorem ipsum",
                "NewPassword"
        );

        mockMvc.perform(patch("/api/users/change-password")
                        .cookie(authCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        Users notUpdatedUser = usersRepository.findByEmail(STUDENT_EMAIL).get();
        assertThat(passwordEncoder.matches(MIGRATION_PASSWORD, notUpdatedUser.getPassword())).isTrue();
    }


    @Test
    void shouldAllowAdminToAssignRole() throws Exception {
        Cookie adminCookie = generateAuthCookie(ADMIN_EMAIL, "Admin");
        UUID targetUserId = getUserIdByEmail(STUDENT_EMAIL);

        mockMvc.perform(post("/api/users/{userId}/roles/{roleCode}", targetUserId, RoleCode.DYREKTOR)
                        .cookie(adminCookie)
                        .param("reason", "Test API"))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Users userFromDb = usersRepository.findById(targetUserId).get();
        boolean hasRole = userFromDb.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getRoleCode() == RoleCode.DYREKTOR);
        assertThat(hasRole).isTrue();
    }

    @Test
    void shouldForbidStudentFromAssigningRole() throws Exception {
        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        UUID targetUserId = getUserIdByEmail(TARGET_EMAIL);

        mockMvc.perform(post("/api/users/{userId}/roles/{roleCode}", targetUserId, RoleCode.ADMINISTRATOR)
                        .cookie(studentCookie))
                .andExpect(status().isForbidden());

        Users userFromDb = usersRepository.findById(targetUserId).get();
        boolean hasRole = userFromDb.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getRoleCode() == RoleCode.ADMINISTRATOR);
        assertThat(hasRole).isFalse();
    }

    @Test
    void shouldAllowAdminToRemoveRole() throws Exception {
        Cookie adminCookie = generateAuthCookie(ADMIN_EMAIL, "Admin");
        UUID targetUserId = getUserIdByEmail(TARGET_EMAIL);


        mockMvc.perform(post("/api/users/{userId}/roles/{roleCode}", targetUserId, RoleCode.NAUCZYCIEL)
                        .cookie(adminCookie)
                        .param("reason", "Setup for removal"))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/api/users/{userId}/roles/{roleCode}", targetUserId, RoleCode.NAUCZYCIEL)
                        .cookie(adminCookie)
                        .param("reason", "Cleaning up"))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Users userFromDb = usersRepository.findById(targetUserId).get();
        boolean hasRole = userFromDb.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getRoleCode() == RoleCode.NAUCZYCIEL);

        assertThat(hasRole).isFalse();
    }

    @Test
    void shouldReturnErrorWhenAdminRemovesRoleUserDoesNotHave() throws Exception {
        Cookie adminCookie = generateAuthCookie(ADMIN_EMAIL, "Admin");
        UUID targetUserId = getUserIdByEmail(TARGET_EMAIL);

        mockMvc.perform(delete("/api/users/{userId}/roles/{roleCode}", targetUserId, RoleCode.DYREKTOR)
                        .cookie(adminCookie)
                        .param("reason", "Test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void shouldForbidStudentFromRemovingRole() throws Exception {
        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        UUID targetUserId = getUserIdByEmail(TARGET_EMAIL);

        mockMvc.perform(delete("/api/users/{userId}/roles/{roleCode}", targetUserId, RoleCode.UCZEN)
                        .cookie(studentCookie)
                        .param("reason", "Test"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAdminToEditUser() throws Exception {
        Cookie adminCookie = generateAuthCookie(ADMIN_EMAIL, "Admin");
        UUID targetUserId = getUserIdByEmail(TARGET_EMAIL);

        UserUpdateRequestDto updateRequest = UserUpdateRequestDto.builder()
                .fullName("Updated Student")
                .status(StatusEnum.CONFIRMED)
                .authProvider(AuthProvider.LOCAL)
                .build();

        mockMvc.perform(put("/api/users/{userId}", targetUserId)
                        .cookie(adminCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        entityManager.flush();
        entityManager.clear();

        Users updatedUser = usersRepository.findById(targetUserId).get();
        assertThat(updatedUser.getFullName()).isEqualTo("Updated Student");
    }

    @Test
    void shouldForbidStudentFromEditingOtherUser() throws Exception {
        Cookie studentCookie = generateAuthCookie(STUDENT_EMAIL, "Uczen");
        UUID targetUserId = getUserIdByEmail(TARGET_EMAIL);

        UserRequestDto updateRequest = UserRequestDto.builder()
                .fullName("Test Name")
                .email("test@school.edu")
                .build();

        mockMvc.perform(put("/api/users/{userId}", targetUserId)
                        .cookie(studentCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        entityManager.clear();
        Users userFromDb = usersRepository.findById(targetUserId).get();
        assertThat(userFromDb.getEmail()).isEqualTo(TARGET_EMAIL);
    }


    private Cookie generateAuthCookie(String email, String fullName) {
        String token = jwtService.generateToken(email, fullName);
        return new Cookie("accessToken", token);
    }

    private UUID getUserIdByEmail(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email))
                .getId();
    }
}