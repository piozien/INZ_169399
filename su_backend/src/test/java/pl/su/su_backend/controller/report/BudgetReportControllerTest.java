package pl.su.su_backend.controller.report;

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
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.model.budget.CouncilBudget;
import pl.su.su_backend.repositories.budget.CouncilBudgetRepository;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.permission.PermissionRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.JwtService;
import pl.su.su_backend.testsupport.Fixtures;
import pl.su.su_backend.testsupport.OAuth2TestConfig;
import pl.su.su_backend.testsupport.RolePermissionTestHelper;
import pl.su.su_backend.dto.report.ReportRequestDto;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(OAuth2TestConfig.class)
class BudgetReportControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Flyway flyway;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private CouncilRepository councilRepository;

    @Autowired
    private CouncilBudgetRepository councilBudgetRepository;

    @Autowired
    private JwtService jwtService;

    private Users teacherUser;
    private Users opiekunUser;
    private String teacherToken;
    private String opiekunToken;
    private CouncilBudget councilBudget;

    @BeforeEach
    void setUp() {
        flyway.migrate();

        userRoleRepository.deleteAll();
        councilBudgetRepository.deleteAll();
        councilRepository.deleteAll();
        usersRepository.deleteAll();

        var teacherRole = RolePermissionTestHelper.ensureRole(
                roleRepository, permissionRepository, RoleCode.NAUCZYCIEL,
                PermissionCode.REPORT_GENERATE
        );

        var opiekunRole = RolePermissionTestHelper.ensureRole(
                roleRepository, permissionRepository, RoleCode.OPIEKUN_SU,
                PermissionCode.COUNCIL_BUDGET_VIEW,
                PermissionCode.REPORT_VIEW
        );

        teacherUser = Fixtures.userWithStatusNoId("Nauczyciel", "nauczyciel@school.test", StatusEnum.CONFIRMED);
        teacherUser.setAuthProvider(AuthProvider.LOCAL);
        teacherUser = usersRepository.save(teacherUser);
        usersRepository.flush();

        opiekunUser = Fixtures.userWithStatusNoId("Opiekun", "opiekun@school.test", StatusEnum.CONFIRMED);
        opiekunUser.setAuthProvider(AuthProvider.LOCAL);
        opiekunUser = usersRepository.save(opiekunUser);
        usersRepository.flush();

        userRoleRepository.save(Fixtures.userRole(teacherUser, teacherRole));
        userRoleRepository.save(Fixtures.userRole(opiekunUser, opiekunRole));
        userRoleRepository.flush();

        teacherToken = jwtService.generateToken(teacherUser.getEmail());
        opiekunToken = jwtService.generateToken(opiekunUser.getEmail());

        Council council = Council.builder()
                .name("SU 2025")
                .academicYear("2025/2026")
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(10))
                .joinCode("JOIN-CODE-123")
                .build();
        council = councilRepository.save(council);

        councilBudget = CouncilBudget.builder()
                .council(council)
                .year("2025")
                .initialAmount(new BigDecimal("500.00"))
                .createdBy(opiekunUser)
                .build();
        councilBudget = councilBudgetRepository.save(councilBudget);
        councilBudgetRepository.flush();
    }

    @Test
    void generateCouncilBudgetReport_ShouldReturnOk_WhenOpiekunHasPermission() {
        ReportRequestDto body = ReportRequestDto.builder()
                .fromDate(LocalDate.parse("2025-01-01"))
                .toDate(LocalDate.parse("2025-12-31"))
                .includeTransactions(true)
                .reportType("SUMMARY")
                .build();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/reports/council-budgets/" + councilBudget.getId(),
                Fixtures.httpEntityWithToken(opiekunToken, body),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("COUNCIL");
    }

    @Test
    void generateQuickCouncilBudgetReport_ShouldReturnOk() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/reports/council-budgets/" + councilBudget.getId() + "/quick",
                HttpMethod.GET,
                Fixtures.httpEntityWithToken(opiekunToken),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}


