package pl.su.su_backend.controller.council;

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
import org.flywaydb.core.Flyway;
import pl.su.su_backend.service.auth.JwtService;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.enums.AuthProvider;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.enums.StatusEnum;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.council.CouncilMemberRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.repositories.user.UserRoleRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.repositories.permission.PermissionRepository;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.testsupport.Fixtures;
import pl.su.su_backend.dto.council.CouncilRequestDto;
import pl.su.su_backend.testsupport.OAuth2TestConfig;
import pl.su.su_backend.testsupport.RolePermissionTestHelper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(OAuth2TestConfig.class)
class CouncilControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RoleRepository roleRepository;

	@Autowired
	private UserRoleRepository userRoleRepository;

    @Autowired
    private CouncilRepository councilRepository;

	@Autowired
	private CouncilMemberRepository councilMemberRepository;

    @Autowired
    private JwtService jwtService;

	@Autowired
	private PermissionRepository permissionRepository;

	private Users opiekunUser;
    private Users przewodniczacyUser;
	private Users uczenUser;
	private String opiekunToken;
	private String przewodniczacyToken;
	private String uczenToken;
    private Council testCouncil;

	@Autowired
	private Flyway flyway;

	@BeforeEach
    void setUp() {
		flyway.migrate();

		userRoleRepository.deleteAll();
        councilRepository.deleteAll();
        usersRepository.deleteAll();

		Role opiekunRole = RolePermissionTestHelper.ensureRole(roleRepository, permissionRepository, RoleCode.OPIEKUN_SU,
				PermissionCode.COUNCIL_VIEW,
				PermissionCode.COUNCIL_VIEW_ALL,
				PermissionCode.COUNCIL_CREATE,
				PermissionCode.COUNCIL_EDIT,
				PermissionCode.COUNCIL_MEMBER_MANAGE,
				PermissionCode.COUNCIL_JOIN);
		Role uczenRole = RolePermissionTestHelper.ensureRole(roleRepository, permissionRepository, RoleCode.UCZEN,
				PermissionCode.COUNCIL_VIEW,
				PermissionCode.COUNCIL_JOIN);

		RolePermissionTestHelper.ensureRole(roleRepository, permissionRepository, RoleCode.PRZEWODNICZACY_SU,
				PermissionCode.COUNCIL_VIEW,
				PermissionCode.COUNCIL_MEMBER_MANAGE,
				PermissionCode.COUNCIL_JOIN);

        opiekunUser = Fixtures.createUserWithRole(usersRepository, userRoleRepository,
                "Opiekun", "opiekun@test.local", StatusEnum.CONFIRMED, AuthProvider.LOCAL, opiekunRole);
        przewodniczacyUser = Fixtures.createUserWithRole(usersRepository, userRoleRepository,
                "Przewodniczacy", "przewodniczacy@test.local", StatusEnum.CONFIRMED, AuthProvider.LOCAL, uczenRole);
        uczenUser = Fixtures.createUserWithRole(usersRepository, userRoleRepository,
                "Uczen", "uczen@test.local", StatusEnum.CONFIRMED, AuthProvider.LOCAL, uczenRole);

		opiekunToken = jwtService.generateToken(opiekunUser.getEmail());
		przewodniczacyToken = jwtService.generateToken(przewodniczacyUser.getEmail());
		uczenToken = jwtService.generateToken(uczenUser.getEmail());

        testCouncil = Fixtures.councilNoId("Test Council", "2025/26", 
                LocalDate.now(), LocalDate.now().plusMonths(6));
        testCouncil.setJoinCode("SU20250001");
        testCouncil.setIsActive(true);
        testCouncil = councilRepository.save(testCouncil);

		CouncilMember przewodniczacyMember = new CouncilMember();
		CouncilMember.CouncilMemberId idComposite = new CouncilMember.CouncilMemberId(testCouncil.getId(), przewodniczacyUser.getId());
		przewodniczacyMember.setId(idComposite);
		przewodniczacyMember.setCouncil(testCouncil);
		przewodniczacyMember.setUser(przewodniczacyUser);
		przewodniczacyMember.setRole(RoleCode.PRZEWODNICZACY_SU);
		councilMemberRepository.save(przewodniczacyMember);
		
		councilRepository.flush();
    }

    @Test
    void createCouncil_ShouldReturnCreated_WhenOpiekunHasPermission() {
        CouncilRequestDto request = Fixtures.councilRequestDto("Samorzad Uczniowski 2025/26", "2025/26",
                LocalDate.parse("2025-12-01"), LocalDate.parse("2026-06-30"));

		ResponseEntity<String> response = restTemplate.postForEntity(
				"/api/council",
				Fixtures.httpEntityWithToken(opiekunToken, request),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).contains("Samorzad Uczniowski 2025/26");
    }

    @Test
    void createCouncil_ShouldReturnForbidden_WhenPrzewodniczacyNoPermission() {
        CouncilRequestDto request = Fixtures.councilRequestDto("Samorzad Uczniowski 2025/26", "2025/26",
                LocalDate.parse("2025-12-01"), LocalDate.parse("2026-06-30"));

		ResponseEntity<String> response = restTemplate.postForEntity(
				"/api/council",
				Fixtures.httpEntityWithToken(przewodniczacyToken, request),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void createCouncil_ShouldReturnForbidden_WhenUczenNoPermission() {
        CouncilRequestDto request = Fixtures.councilRequestDto("Samorzad Uczniowski 2025/26", "2025/26",
                LocalDate.parse("2025-12-01"), LocalDate.parse("2026-06-30"));

		ResponseEntity<String> response = restTemplate.postForEntity(
				"/api/council",
				Fixtures.httpEntityWithToken(uczenToken, request),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
	void getCouncil_ShouldReturnOk_WhenOpiekunHasPermission() {
		ResponseEntity<String> response = restTemplate.exchange(
				"/api/council",
				HttpMethod.GET,
				Fixtures.httpEntityWithToken(opiekunToken),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("Test Council");
	}

    @Test
	void getCouncil_ShouldReturnOk_WhenUczenHasPermission() {
		// Add uczen as council member
		CouncilMember uczenMember = new CouncilMember();
		CouncilMember.CouncilMemberId uczenMemberId = new CouncilMember.CouncilMemberId(testCouncil.getId(), uczenUser.getId());
		uczenMember.setId(uczenMemberId);
		uczenMember.setCouncil(testCouncil);
		uczenMember.setUser(uczenUser);
		uczenMember.setRole(RoleCode.CZLONEK_SU);
		councilMemberRepository.save(uczenMember);

		ResponseEntity<String> response = restTemplate.exchange(
				"/api/council",
				HttpMethod.GET,
				Fixtures.httpEntityWithToken(uczenToken),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("Test Council");
    }

    @Test
	void getCouncilById_ShouldReturnOk_WhenOpiekunHasPermission() {
		ResponseEntity<String> response = restTemplate.exchange(
				"/api/council/" + testCouncil.getId(),
                HttpMethod.GET,
				Fixtures.httpEntityWithToken(opiekunToken),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Test Council");
    }

    @Test
	void joinCouncilByCode_ShouldReturnOk_WhenUczenHasPermission() {
		ResponseEntity<String> response = restTemplate.postForEntity(
				"/api/council/join/" + testCouncil.getJoinCode(),
				Fixtures.httpEntityWithToken(uczenToken),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Test Council");
    }

    @Test
	void joinCouncilByCode_ShouldReturnBadRequest_WhenInvalidCode() {
		ResponseEntity<String> response = restTemplate.postForEntity(
				"/api/council/join/INVALID_CODE",
				Fixtures.httpEntityWithToken(uczenToken),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
	void joinCouncilByCode_ShouldReturnBadRequest_WhenUserAlreadyMember() {
		// Add uczen as council member
		CouncilMember uczenMember = new CouncilMember();
		CouncilMember.CouncilMemberId uczenMemberId = new CouncilMember.CouncilMemberId(testCouncil.getId(), uczenUser.getId());
		uczenMember.setId(uczenMemberId);
		uczenMember.setCouncil(testCouncil);
		uczenMember.setUser(uczenUser);
		uczenMember.setRole(RoleCode.CZLONEK_SU);
		councilMemberRepository.save(uczenMember);

		ResponseEntity<String> response = restTemplate.postForEntity(
				"/api/council/join/" + testCouncil.getJoinCode(),
				Fixtures.httpEntityWithToken(uczenToken),
				String.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).contains("already a member");
	}
}
