package pl.su.su_backend.service.council;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.exception.ErrorCode;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCategory;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilMemberRepository;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouncilMemberService {

    private final CouncilMemberRepository councilMemberRepository;
    private final CouncilRepository councilRepository;
    private final UsersRepository usersRepository;
    private final PermissionService permissionService;
    private final ActivityLogService activityLogService;

    public CouncilMember addMemberWithRole(UUID councilId, UUID userId, RoleCode roleCode, String actingUserEmail) {
        log.info("Adding member {} to council {} with role {} by user: {}", userId, councilId, roleCode, actingUserEmail);

        Users actingUser = usersRepository.findByEmail(actingUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "Nie znaleziono użytkownika"));

        if (!permissionService.hasPermission(actingUser.getId(), PermissionCode.COUNCIL_MEMBER_MANAGE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Nie masz uprawnień do zarządzania członkami samorządu.");
        }

        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Nie znaleziono samorządu"));

        Users member = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "Nie znaleziono użytkownika"));

        CouncilMember.CouncilMemberId membershipId = new CouncilMember.CouncilMemberId(councilId, userId);
        if (councilMemberRepository.existsById(membershipId)) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Użytkownik jest już członkiem tego samorządu.");
        }

        if (roleCode.getCategory() != RoleCategory.SU) {
            throw ApiException.badRequest(ErrorCode.INVALID_ROLE_ASSIGNMENT, 
                "Członkom rady można przypisywać wyłącznie role SU. W przypadku innych ról skontaktuj się z Dyrekcją!");
        }

        CouncilMember councilMember = new CouncilMember();
        councilMember.setId(membershipId);
        councilMember.setCouncil(council);
        councilMember.setUser(member);
        councilMember.setRole(roleCode);

        CouncilMember savedMember = councilMemberRepository.save(councilMember);

        activityLogService.log(actingUser.getId(), ActionType.USER_UPDATED,
                "Dodano " + member.getFullName() + " do samorządu " + council.getName() + " z rolą " + roleCode.getDisplayName());

        log.info("Member {} added to council {} with role {}", member.getEmail(), council.getName(), roleCode);

        return savedMember;
    }

    public CouncilMember updateMemberRole(UUID councilId, UUID userId, RoleCode newRoleCode, String actingUserEmail) {
        log.info("Updating member {} role in council {} to {} by user: {}", userId, councilId, newRoleCode, actingUserEmail);

        Users actingUser = usersRepository.findByEmail(actingUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "Nie znaleziono użytkownika"));

        if (!permissionService.hasPermission(actingUser.getId(), PermissionCode.COUNCIL_MEMBER_MANAGE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Nie masz uprawnień do zarządzania członkami samorządu.");
        }

        CouncilMember.CouncilMemberId membershipId = new CouncilMember.CouncilMemberId(councilId, userId);
        CouncilMember councilMember = councilMemberRepository.findById(membershipId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Nie znaleziono członka samorządu"));

        if (newRoleCode.getCategory() != RoleCategory.SU) {
            throw ApiException.badRequest(ErrorCode.INVALID_ROLE_ASSIGNMENT,
                    "Członkom rady można przypisywać wyłącznie role SU. W przypadku innych ról skontaktuj się z Dyrekcją!");
        }

        RoleCode oldRole = councilMember.getRole();
        councilMember.setRole(newRoleCode);

        CouncilMember updatedMember = councilMemberRepository.save(councilMember);

        activityLogService.log(actingUser.getId(), ActionType.USER_UPDATED,
                "Aktualizowanie " + councilMember.getUser().getFullName() + " roli w samorządzie, z "
                + oldRole.getDisplayName() + " na " + newRoleCode.getDisplayName());

        log.info("Member {} role updated from {} to {}", councilMember.getUser().getEmail(), oldRole, newRoleCode);

        return updatedMember;
    }

    public void removeMember(UUID councilId, UUID userId, String actingUserEmail) {
        log.info("Removing member {} from council {} by user: {}", userId, councilId, actingUserEmail);

        Users actingUser = usersRepository.findByEmail(actingUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "Nie znaleziono użytkownika"));

        if (!permissionService.hasPermission(actingUser.getId(), PermissionCode.COUNCIL_MEMBER_MANAGE)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Nie masz uprawnień do zarządzania członkami samorządu.");
        }

        CouncilMember.CouncilMemberId membershipId = new CouncilMember.CouncilMemberId(councilId, userId);
        CouncilMember councilMember = councilMemberRepository.findById(membershipId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Nie znaleziono członka samorządu"));

        String memberName = councilMember.getUser().getFullName();
        String councilName = councilMember.getCouncil().getName();

        councilMemberRepository.delete(councilMember);

        activityLogService.log(actingUser.getId(), ActionType.USER_UPDATED,
                "Usunięto " + memberName + " z samorządu " + councilName);

        log.info("Member {} removed from council {}", memberName, councilName);
    }

    @Transactional(readOnly = true)
    public List<CouncilMember> getCouncilMembers(UUID councilId, String actingUserEmail) {
        log.info("Fetching members of council {} by user: {}", councilId, actingUserEmail);

        Users actingUser = usersRepository.findByEmail(actingUserEmail)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "Nie znaleziono użytkownika"));

        if (!permissionService.hasPermission(actingUser.getId(), PermissionCode.COUNCIL_VIEW)) {
            throw ApiException.forbidden(ErrorCode.ACCESS_DENIED, "Nie masz uprawnień do przeglądania członków samorządu.");
        }

        if (!councilRepository.existsById(councilId)) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Nie znaleziono samorządu");
        }

        return councilMemberRepository.findByCouncilId(councilId);
    }

    @Transactional(readOnly = true)
    public List<CouncilMember> getCouncilMembersInternal(UUID councilId) {
        return councilMemberRepository.findByCouncilId(councilId);
    }

    @Transactional(readOnly = true)
    public List<CouncilMember> getUserCouncilMemberships(UUID userId) {
        log.info("Fetching council memberships for user: {}", userId);

        if (!usersRepository.existsById(userId)) {
            throw ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "Nie znaleziono użytkownika");
        }

        return councilMemberRepository.findByIdUserId(userId);
    }

    @Transactional(readOnly = true)
    public boolean isMemberOfCouncil(UUID userId, UUID councilId) {
        CouncilMember.CouncilMemberId membershipId = new CouncilMember.CouncilMemberId(councilId, userId);
        return councilMemberRepository.existsById(membershipId);
    }

    public void joinCouncilAsBasicMember(UUID councilId, UUID userId) {
        log.info("User {} joining council {} as basic member", userId, councilId);

        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Council not found"));

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.badRequest(ErrorCode.USER_NOT_FOUND, "Nie znaleziono użytkownika"));

        CouncilMember.CouncilMemberId membershipId = new CouncilMember.CouncilMemberId(councilId, userId);
        if (councilMemberRepository.existsById(membershipId)) {
            throw ApiException.badRequest(ErrorCode.VALIDATION_ERROR, "Użytkownik jest już członkiem tej rady.");
        }

        CouncilMember councilMember = new CouncilMember();
        councilMember.setId(membershipId);
        councilMember.setCouncil(council);
        councilMember.setUser(user);
        councilMember.setRole(RoleCode.CZLONEK_SU);

        councilMemberRepository.save(councilMember);

        log.info("User {} joined council {} as {}", user.getEmail(), council.getName(), RoleCode.CZLONEK_SU);

    }
}

