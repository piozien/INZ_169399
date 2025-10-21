package pl.su.su_backend.testsupport;

import pl.su.su_backend.dto.classes.ClassesRequestDto;
import pl.su.su_backend.dto.classes.ClassesResponseDto;
import pl.su.su_backend.dto.event.EventRequestDto;
import pl.su.su_backend.dto.event.EventResponseDto;
import pl.su.su_backend.dto.suggestion.SuggestionRequestDto;
import pl.su.su_backend.dto.user.LoginRequestDto;
import pl.su.su_backend.dto.user.LoginResponseDto;
import pl.su.su_backend.dto.user.RefreshTokenRequestDto;
import pl.su.su_backend.dto.user.UserRequestDto;
import pl.su.su_backend.dto.user.UserResponseDto;
import pl.su.su_backend.model.budget.*;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.enums.*;
import pl.su.su_backend.model.event.Event;
import java.util.Set;
import pl.su.su_backend.model.event.EventParticipant;
import pl.su.su_backend.model.log.ActivityLog;
import pl.su.su_backend.model.permissions.Permission;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.suggestion.Suggestion;
import pl.su.su_backend.model.suggestion.SuggestionTag;
import pl.su.su_backend.model.users.PasswordResetToken;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class Fixtures {

    private Fixtures() {
    }


    // ===== USER METHODS =====
    
    public static Users user() {
        return user("Jan Kowalski", "jan.kowalski@test.com");
    }
    
    public static Users user(String fullName, String email) {
        Users u = Users.builder()
                .fullName(fullName)
                .email(email)
                .password("encodedPassword")
                .status(StatusEnum.PENDING)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .build();
        u.setId(UUID.randomUUID());
        return u;
    }
    
    public static Users simpleUser(String fullName, String email) {
        Users u = Users.builder()
                .fullName(fullName)
                .email(email)
                .password("pass")
                .build();
        u.setId(UUID.randomUUID());
        return u;
    }
    
    public static Users userWithStatus(String fullName, String email, StatusEnum status) {
        Users user = Users.builder()
                .fullName(fullName)
                .email(email)
                .password("password123")
                .status(status)
                .build();
        user.setId(UUID.randomUUID());
        return user;
    }

    public static Users userWithClass(String fullName, String email, Classes schoolClass) {
        Users user = Users.builder()
                .fullName(fullName)
                .email(email)
                .password("password123")
                .classes(schoolClass)
                .build();
        user.setId(UUID.randomUUID());
        return user;
    }

    public static PasswordResetToken passwordResetToken(Users user, String token, LocalDateTime expiresAt) {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        resetToken.setId(UUID.randomUUID());
        return resetToken;
    }

    public static UserRole userRole(Users user, Role role) {
        UserRole.Id id = new UserRole.Id(user.getId(), role.getId());
        UserRole userRole = UserRole.builder()
                .id(id)
                .user(user)
                .role(role)
                .build();
        return userRole;
    }

    // ===== CLASS METHODS =====

    public static Classes schoolClass(String name, String year) {
        Classes c = Classes.builder()
                .name(name)
                .year(year)
                .build();
        c.setId(UUID.randomUUID());
        return c;
    }

    public static ClassesRequestDto classesRequestDto() {
        return ClassesRequestDto.builder()
                .name("1A")
                .year("2024")
                .build();
    }

    public static ClassesRequestDto classesRequestDto(String name, String year) {
        return ClassesRequestDto.builder()
                .name(name)
                .year(year)
                .build();
    }

    public static ClassesResponseDto classesResponseDto() {
        return ClassesResponseDto.builder()
                .id(UUID.randomUUID())
                .name("1A")
                .year("2024")
                .build();
    }

    public static ClassBudget classBudget(Classes classes, BigDecimal initialAmount, Users createdBy){
        ClassBudget cb = ClassBudget.builder()
                .classes(classes)
                .initialAmount(initialAmount)
                .createdBy(createdBy)
                .build();
        cb.setId(UUID.randomUUID());
        return cb;
    }

    public static ClassTransaction classTransaction(ClassBudget budget, TransactionType type, BigDecimal amount,
                                                    String description, Users addedBy) {
        ClassTransaction transaction = ClassTransaction.builder()
                .budget(budget)
                .type(type)
                .amount(amount)
                .description(description)
                .addedBy(addedBy)
                .build();
        transaction.setId(UUID.randomUUID());
        return transaction;
    }

    public static ClassBudget completeClassBudget(Classes schoolClass, String year, BigDecimal initialAmount,
                                                  Users createdBy) {
        ClassBudget budget = classBudget(schoolClass, initialAmount, createdBy);
        budget.setYear(year);
        budget.onCreate();

        ClassTransaction income = classTransaction(budget, TransactionType.INCOME, new BigDecimal("100.00"),
                "Initial funding", createdBy);
        ClassTransaction expense = classTransaction(budget, TransactionType.EXPENSE, new BigDecimal("50.00"),
                "First expense", createdBy);
        
        budget.getTransactions().add(income);
        budget.getTransactions().add(expense);
        
        return budget;
    }

    // ===== COUNCIL METHODS =====

    public static Council createCouncil(String name, String academicYear, LocalDate start, LocalDate end){
        Council council = Council.builder()
                .name(name)
                .academicYear(academicYear)
                .startDate(start)
                .endDate(end).build();
        council.setId(UUID.randomUUID());
        return council;
    }

    public static CouncilBudget councilBudget(Council council, String year, BigDecimal initialAmount, Users createdBy) {
        CouncilBudget budget = CouncilBudget.builder()
                .council(council)
                .year(year)
                .initialAmount(initialAmount)
                .createdBy(createdBy)
                .build();
        budget.setId(UUID.randomUUID());
        return budget;
    }

    public static CouncilTransaction councilTransaction(CouncilBudget budget, TransactionType type, BigDecimal amount,
                                                        String description, Users addedBy) {
        CouncilTransaction transaction = CouncilTransaction.builder()
                .budget(budget)
                .type(type)
                .amount(amount)
                .description(description)
                .date(LocalDateTime.now())
                .addedBy(addedBy)
                .build();
        transaction.setId(UUID.randomUUID());
        return transaction;
    }

    public static CouncilBudget completeCouncilBudget(Council council, String year, BigDecimal initialAmount,
                                                      Users createdBy) {
        CouncilBudget budget = councilBudget(council, year, initialAmount, createdBy);
        budget.onCreate();

        CouncilTransaction income = councilTransaction(budget, TransactionType.INCOME, new BigDecimal("500.00"),
                "Council funding", createdBy);
        CouncilTransaction expense = councilTransaction(budget, TransactionType.EXPENSE, new BigDecimal("200.00"),
                "Council expense", createdBy);
        
        budget.getTransactions().add(income);
        budget.getTransactions().add(expense);
        
        return budget;
    }

    // ===== EVENT METHODS =====

    public static Event simpleEvent(String title, String description, LocalDateTime start, LocalDateTime end) {
        Event event = Event.builder()
                .title(title)
                .description(description)
                .startDate(start)
                .endDate(end)
                .build();
        event.setId(UUID.randomUUID());
        return event;
    }

    public static Event eventWithCreator(String title, String description, LocalDateTime start, LocalDateTime end, Users creator) {
        Event event = Event.builder()
                .title(title)
                .description(description)
                .startDate(start)
                .endDate(end)
                .createdBy(creator)
                .status(EventStatus.DRAFT)
                .build();
        event.setId(UUID.randomUUID());
        return event;
    }

    public static EventParticipant eventParticipant(Event event, Users user, EventParticipantRole role) {
        EventParticipant.Id id = new EventParticipant.Id(event.getId(), user.getId());
        return EventParticipant.builder()
                .id(id)
                .event(event)
                .user(user)
                .role(role)
                .confirmed(false)
                .build();
    }

    // ===== SUGGESTION METHODS =====

    public static Suggestion suggestion(Users user, String title, String description, SuggestionStatus status) {
        Suggestion suggestion = Suggestion.builder()
                .user(user)
                .title(title)
                .description(description)
                .status(status)
                .isAnonymous(false)
                .build();
        suggestion.setId(UUID.randomUUID());
        return suggestion;
    }

    public static SuggestionTag suggestionTag(Suggestion suggestion, String tagName) {
        SuggestionTag.Id id = new SuggestionTag.Id(suggestion.getId(), tagName);
        SuggestionTag tag = SuggestionTag.builder()
                .id(id)
                .suggestion(suggestion)
                .build();
        return tag;
    }


    // ===== ROLE & PERMISSION METHODS =====

    public static Role role(RoleCode roleCode) {
        return role(roleCode, "Test " + roleCode.name() + " role");
    }

    public static Role role(RoleCode roleCode, String description) {
        Role role = Role.builder()
                .roleCode(roleCode)
                .description(description)
                .build();
        role.setId(UUID.randomUUID());
        return role;
    }

    public static Permission permission(String name, String description) {
        Permission permission = Permission.builder()
                .name(name)
                .description(description)
                .build();
        permission.setId(UUID.randomUUID());
        return permission;
    }

    // ===== LOG METHODS =====

    public static ActivityLog activityLog(Users user, ActionType actionType, String action) {
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .actionType(actionType)
                .action(action)
                .build();
        log.setId(UUID.randomUUID());
        return log;
    }

    // ===== DTO METHODS =====
    
    public static UserRequestDto userRequestDto() {
        return UserRequestDto.builder()
                .fullName("Jan Kowalski")
                .email("jan.kowalski@test.com")
                .password("password123")
                .authProvider(AuthProvider.LOCAL)
                .build();
    }
    
    public static UserRequestDto userRequestDto(String fullName, String email, String password) {
        return UserRequestDto.builder()
                .fullName(fullName)
                .email(email)
                .password(password)
                .authProvider(AuthProvider.LOCAL)
                .build();
    }
    
    public static LoginRequestDto loginRequestDto() {
        return LoginRequestDto.builder()
                .email("jan.kowalski@test.com")
                .password("password123")
                .build();
    }
    
    public static LoginRequestDto loginRequestDto(String email, String password) {
        return LoginRequestDto.builder()
                .email(email)
                .password(password)
                .build();
    }
    
    public static RefreshTokenRequestDto refreshTokenRequestDto() {
        return RefreshTokenRequestDto.builder()
                .refreshToken("test-refresh-token")
                .build();
    }
    
    public static RefreshTokenRequestDto refreshTokenRequestDto(String refreshToken) {
        return RefreshTokenRequestDto.builder()
                .refreshToken(refreshToken)
                .build();
    }
    
    public static UserResponseDto userResponseDto() {
        return UserResponseDto.builder()
                .id(UUID.randomUUID())
                .fullName("Jan Kowalski")
                .email("jan.kowalski@test.com")
                .status(StatusEnum.PENDING)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    public static LoginResponseDto loginResponseDto() {
        return LoginResponseDto.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .user(userResponseDto())
                .roles(List.of("UCZEN"))
                .build();
    }

    public static SuggestionRequestDto suggestionRequestDto() {
        return SuggestionRequestDto.builder()
                .userId(UUID.randomUUID())
                .title("Test Suggestion")
                .description("This is a test suggestion")
                .isAnonymous(false)
                .status(SuggestionStatus.PENDING)
                .tags(Set.of("test", "example"))
                .build();
    }
    
    public static SuggestionRequestDto suggestionRequestDto(UUID userId, String title, String description, Boolean isAnonymous) {
        return SuggestionRequestDto.builder()
                .userId(userId)
                .title(title)
                .description(description)
                .isAnonymous(isAnonymous)
                .status(SuggestionStatus.PENDING)
                .tags(Set.of("test"))
                .build();
    }

    public static EventRequestDto eventRequestDto() {
        return EventRequestDto.builder()
                .title("Test Event")
                .description("This is a test event")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Test Location")
                .calendarEventId("test-calendar-id")
                .build();
    }
    
    public static EventRequestDto eventRequestDto(String title, String description, LocalDateTime startDate, LocalDateTime endDate) {
        return EventRequestDto.builder()
                .title(title)
                .description(description)
                .startDate(startDate)
                .endDate(endDate)
                .location("Test Location")
                .calendarEventId("test-calendar-id")
                .build();
    }
    
    public static EventResponseDto eventResponseDto() {
        return EventResponseDto.builder()
                .id(UUID.randomUUID())
                .title("Test Event")
                .description("This is a test event")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Test Location")
                .createdById(UUID.randomUUID())
                .calendarEventId("test-calendar-id")
                .createdAt(LocalDateTime.now())
                .status(EventStatus.DRAFT)
                .build();
    }
    
    public static EventResponseDto eventResponseDto(UUID id, String title, String description, EventStatus status) {
        return EventResponseDto.builder()
                .id(id)
                .title(title)
                .description(description)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(1).plusHours(2))
                .location("Test Location")
                .createdById(UUID.randomUUID())
                .calendarEventId("test-calendar-id")
                .createdAt(LocalDateTime.now())
                .status(status)
                .build();
    }
}
