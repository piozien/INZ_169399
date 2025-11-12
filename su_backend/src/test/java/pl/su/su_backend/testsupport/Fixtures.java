package pl.su.su_backend.testsupport;

import pl.su.su_backend.dto.classes.ClassesRequestDto;
import pl.su.su_backend.dto.event.EventRequestDto;
import pl.su.su_backend.dto.council.CouncilRequestDto;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.dto.suggestion.SuggestionRequestDto;
import pl.su.su_backend.dto.user.LoginRequestDto;
import pl.su.su_backend.dto.user.RefreshTokenRequestDto;
import pl.su.su_backend.dto.user.UserRequestDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.model.budget.*;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.enums.*;
import pl.su.su_backend.model.event.Event;

import java.util.Set;

import pl.su.su_backend.model.permissions.Permission;
import pl.su.su_backend.model.suggestion.Suggestion;
import pl.su.su_backend.model.suggestion.SuggestionTag;
import pl.su.su_backend.model.users.PasswordResetToken;
import pl.su.su_backend.model.users.Users;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public final class Fixtures {

    private Fixtures() {
    }


    // ===== USER METHODS =====

    public static Users user() {
        return user("Jan Kowalski", "jan.kowalski@test.com");
    }

    public static Users user(String fullName, String email) {
        Users u = Users.builder().fullName(fullName).email(email).password("encodedPassword").status(StatusEnum.PENDING).authProvider(AuthProvider.LOCAL).createdAt(LocalDateTime.now()).build();
        u.setId(UUID.randomUUID());
        return u;
    }

    public static Users simpleUser(String fullName, String email) {
        Users u = Users.builder().fullName(fullName).email(email).password("pass").build();
        u.setId(UUID.randomUUID());
        return u;
    }

    public static Users userWithStatus(String fullName, String email, StatusEnum status) {
        Users user = Users.builder().fullName(fullName).email(email).password("password123").status(status).build();
        user.setId(UUID.randomUUID());
        return user;
    }

    public static Users userWithStatusNoId(String fullName, String email, StatusEnum status) {
        return Users.builder().fullName(fullName).email(email).password("password123").status(status).build();
    }

    public static Council councilNoId(String name, String academicYear, LocalDate start, LocalDate end) {
        return Council.builder().name(name).academicYear(academicYear).startDate(start).endDate(end).build();
    }

    public static PasswordResetToken passwordResetToken(Users user, String token, LocalDateTime expiresAt) {
        PasswordResetToken resetToken = PasswordResetToken.builder().token(token).user(user).expiresAt(expiresAt).used(false).build();
        resetToken.setId(UUID.randomUUID());
        return resetToken;
    }

    public static UserRole userRole(Users user, Role role) {
        UserRole.Id id = new UserRole.Id(user.getId(), role.getId());
        UserRole userRole = UserRole.builder().id(id).user(user).role(role).build();
        return userRole;
    }

    // ===== CLASS METHODS =====

    public static Classes schoolClass(String name, String year) {
        Classes c = Classes.builder().name(name).year(year).build();
        c.setId(UUID.randomUUID());
        return c;
    }

    public static ClassesRequestDto classesRequestDto(String name, String year) {
        return ClassesRequestDto.builder().name(name).year(year).build();
    }

    public static ClassBudget classBudget(Classes classes, BigDecimal initialAmount, Users createdBy) {
        ClassBudget cb = ClassBudget.builder().classes(classes).initialAmount(initialAmount).createdBy(createdBy).build();
        cb.setId(UUID.randomUUID());
        return cb;
    }

    public static ClassTransaction classTransaction(ClassBudget budget, TransactionType type, BigDecimal amount, String description, Users addedBy) {
        ClassTransaction transaction = ClassTransaction.builder().budget(budget).type(type).amount(amount).description(description).addedBy(addedBy).build();
        transaction.setId(UUID.randomUUID());
        return transaction;
    }

    // ===== COUNCIL METHODS =====

    public static Council createCouncil(String name, String academicYear, LocalDate start, LocalDate end) {
        Council council = Council.builder().name(name).academicYear(academicYear).startDate(start).endDate(end).build();
        council.setId(UUID.randomUUID());
        return council;
    }

    // ===== EVENT METHODS =====

    public static Event simpleEvent(String title, String description, LocalDateTime start, LocalDateTime end) {
        Event event = Event.builder().title(title).description(description).startDate(start).endDate(end).build();
        event.setId(UUID.randomUUID());
        return event;
    }

    public static Event eventNoId(String title, String description, LocalDateTime start, LocalDateTime end) {
        return Event.builder().title(title).description(description).startDate(start).endDate(end).build();
    }

    public static Event eventWithCreator(String title, String description, LocalDateTime start, LocalDateTime end, Users creator) {
        Event event = Event.builder().title(title).description(description).startDate(start).endDate(end).createdBy(creator).status(EventStatus.DRAFT).build();
        event.setId(UUID.randomUUID());
        return event;
    }

    // ===== SUGGESTION METHODS =====

    public static Suggestion suggestion(Users user, String title, String description, SuggestionStatus status) {
        Suggestion suggestion = Suggestion.builder().user(user).title(title).description(description).status(status).isAnonymous(false).build();
        suggestion.setId(UUID.randomUUID());
        return suggestion;
    }

    public static SuggestionTag suggestionTag(Suggestion suggestion, String tagName) {
        SuggestionTag.Id id = new SuggestionTag.Id(suggestion.getId(), tagName);
        SuggestionTag tag = SuggestionTag.builder().id(id).suggestion(suggestion).build();
        return tag;
    }


    // ===== ROLE & PERMISSION METHODS =====

    public static Role role(RoleCode roleCode) {
        return role(roleCode, "Test " + roleCode.name() + " role");
    }

    public static Role role(RoleCode roleCode, String description) {
        Role role = Role.builder().roleCode(roleCode).description(description).build();
        role.setId(UUID.randomUUID());
        return role;
    }

    public static Role roleNoId(RoleCode roleCode) {
        return roleNoId(roleCode, "Test " + roleCode.name() + " role");
    }

    public static Role roleNoId(RoleCode roleCode, String description) {
        return Role.builder().roleCode(roleCode).description(description).build();
    }

    public static Permission permission(String name, String description) {
        Permission permission = Permission.builder().name(name).description(description).build();
        permission.setId(UUID.randomUUID());
        return permission;
    }

    public static Permission permissionNoId(String name, String description) {
        return Permission.builder().name(name).description(description).build();
    }

    // ===== DTO METHODS =====

    public static UserRequestDto userRequestDto() {
        return UserRequestDto.builder().fullName("Jan Kowalski").email("jan.kowalski@test.com").password("password123").authProvider(AuthProvider.LOCAL).build();
    }

    public static UserRequestDto userRequestDto(String fullName, String email, String password) {
        return UserRequestDto.builder().fullName(fullName).email(email).password(password).authProvider(AuthProvider.LOCAL).build();
    }

    public static LoginRequestDto loginRequestDto() {
        return LoginRequestDto.builder().email("jan.kowalski@test.com").password("password123").build();
    }

    public static RefreshTokenRequestDto refreshTokenRequestDto(String refreshToken) {
        return RefreshTokenRequestDto.builder().refreshToken(refreshToken).build();
    }

    public static UserResponseDto userResponseDto() {
        return UserResponseDto.builder().id(UUID.randomUUID()).fullName("Jan Kowalski").email("jan.kowalski@test.com").status(StatusEnum.PENDING).authProvider(AuthProvider.LOCAL).createdAt(LocalDateTime.now()).build();
    }

    public static SuggestionRequestDto suggestionRequestDto(UUID userId, String title, String description, Boolean isAnonymous) {
        return SuggestionRequestDto.builder().userId(userId).title(title).description(description).isAnonymous(isAnonymous).status(SuggestionStatus.PENDING).tags(Set.of("test")).build();
    }

    public static EventRequestDto eventRequestDto(String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        return EventRequestDto.builder().title(title).description(description).startDate(startDate).endDate(endDate).location("Test Location").calendarEventId("test-calendar-id").build();
    }

    // ===== HTTP HELPER METHODS =====
    public static HttpEntity<String> httpEntityWithToken(String token, String jsonBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(jsonBody, headers);
    }

    public static HttpEntity<String> httpEntityWithToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    public static HttpEntity<?> httpEntityWithToken(String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    // ===== Council DTO helpers =====
    public static CouncilRequestDto councilRequestDto(String name, String academicYear, LocalDate start, LocalDate end) {
        return CouncilRequestDto.builder().name(name).academicYear(academicYear).startDate(start).endDate(end).build();
    }

    // ===== Persisted user with role helper =====
    public static Users createUserWithRole(
            UsersRepository usersRepository,
            UserRoleRepository userRoleRepository,
            String fullName,
            String email,
            StatusEnum status,
            AuthProvider provider,
            Role role
    ) {
        Users user = userWithStatusNoId(fullName, email, status);
        user.setAuthProvider(provider);
        user = usersRepository.save(user);
        usersRepository.flush();

        UserRole.Id id = new UserRole.Id();
        id.setUserId(user.getId());
        id.setRoleId(role.getId());
        UserRole userRole = new UserRole();
        userRole.setId(id);
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);
        userRoleRepository.flush();

        return usersRepository.findByEmail(email).orElse(user);
    }
}
