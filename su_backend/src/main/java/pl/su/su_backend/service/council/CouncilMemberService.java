package pl.su.su_backend.service.council;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.council.RoleOptionDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCategory;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.roles.Role;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilMemberRepository;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.user.UsersRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;

import java.util.*;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));

        if (!permissionService.hasPermission(actingUser.getId(), PermissionCode.COUNCIL_MEMBER_MANAGE, councilId)) {
            throw ApiException.forbidden("Nie masz uprawnień do dodawania członków w tym samorządzie.");
        }

        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono samorządu"));

        Users member = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika do dodania"));

        CouncilMember.CouncilMemberId membershipId = new CouncilMember.CouncilMemberId(councilId, userId);
        if (councilMemberRepository.existsById(membershipId)) {
            throw ApiException.badRequest("Użytkownik jest już członkiem tego samorządu.");
        }

        if (roleCode.getCategory() != RoleCategory.SU) {
            throw ApiException.badRequest("Członkom rady można przypisywać wyłącznie role SU." +
                    " W przypadku innych ról skontaktuj się z Dyrekcją");
        }

        CouncilMember councilMember = new CouncilMember();
        councilMember.setId(membershipId);
        councilMember.setCouncil(council);
        councilMember.setUser(member);
        councilMember.setRole(roleCode);

        CouncilMember savedMember = councilMemberRepository.save(councilMember);

        activityLogService.log(actingUser.getId(), ActionType.USER_UPDATED,
                "Dodano " + member.getFullName() + " do samorządu " + council.getName());

        return savedMember;
    }

    @Transactional(readOnly = true)
    public List<CouncilMember> getCouncilMembers(UUID councilId, String actingUserEmail) {
        Users actingUser = usersRepository.findByEmail(actingUserEmail)
                .orElseThrow(() -> ApiException.notFound("Użytkownik nie istnieje"));

        boolean hasPermission = permissionService.hasPermission(actingUser.getId(), PermissionCode.COUNCIL_VIEW, councilId);
        boolean isMember = isMemberOfCouncil(actingUser.getId(), councilId);

        if (!hasPermission && !isMember) {
            throw ApiException.forbidden("Brak dostępu do listy członków tego samorządu");
        }

        if (!councilRepository.existsById(councilId)) {
            throw ApiException.notFound("Nie znaleziono samorządu");
        }

        return councilMemberRepository.findByCouncilId(councilId);
    }

    @Transactional
    public void removeMember(UUID councilId, UUID userId, String actingUserEmail) {
        Users actingUser = usersRepository.findByEmail(actingUserEmail)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));

        boolean isSelf = actingUser.getId().equals(userId);

        boolean hasPermissionManage = permissionService.hasPermission(actingUser.getId(),
                PermissionCode.COUNCIL_MEMBER_MANAGE, councilId);

        if (!isSelf && !hasPermissionManage) {
            throw ApiException.forbidden("Brak uprawnień do usuwania członków");
        }

        CouncilMember.CouncilMemberId membershipId = new CouncilMember.CouncilMemberId(councilId, userId);
        CouncilMember targetMember = councilMemberRepository.findById(membershipId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono członka samorządu"));

        if (!isSelf) {
            int actingRank = getCouncilRank(councilId, actingUser);
            int targetRank = targetMember.getRole().getRank();

            if (actingRank <= targetRank) {
                throw ApiException.forbidden("Nie posiadasz wystarczającej rangi, aby usunąć tego członka.");
            }
        }

        councilMemberRepository.delete(targetMember);

        activityLogService.log(actingUser.getId(), ActionType.USER_UPDATED,
                "Usunięto członka z samorządu (ID: " + userId + ")");
    }

    @Transactional
    public CouncilMember updateMemberRole(UUID councilId, UUID userId, RoleCode newRoleCode, String actingUserEmail) {
        Users actingUser = usersRepository.findByEmail(actingUserEmail)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));

        if (!permissionService.hasPermission(actingUser.getId(), PermissionCode.COUNCIL_MEMBER_MANAGE, councilId)) {
            throw ApiException.forbidden("Brak uprawnień do edycji ról");
        }

        CouncilMember.CouncilMemberId membershipId = new CouncilMember.CouncilMemberId(councilId, userId);
        CouncilMember targetMember = councilMemberRepository.findById(membershipId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono członka samorządu"));

        if (newRoleCode.getCategory() != RoleCategory.SU) {
            throw ApiException.badRequest("Można przypisywać tylko role samorządowe.");
        }

        int actingRank = getCouncilRank(councilId, actingUser);
        int targetCurrentRank = targetMember.getRole().getRank();
        int newRoleRank = newRoleCode.getRank();

        if (actingRank <= targetCurrentRank) {
            throw ApiException.forbidden("Nie możesz modyfikować członka o randze równej lub wyższej od Twojej.");
        }

        if (actingRank <= newRoleRank) {
            throw ApiException.forbidden("Nie możesz nadać rangi równej lub wyższej od Twojej własnej.");
        }

        targetMember.setRole(newRoleCode);

        activityLogService.log(actingUser.getId(), ActionType.USER_UPDATED,
                "Zmieniono rolę członka " + targetMember.getUser().getEmail() + " na " + newRoleCode);

        return councilMemberRepository.save(targetMember);
    }

    @Transactional(readOnly = true)
    public List<CouncilMember> getUserCouncilMemberships(UUID userId) {
        return councilMemberRepository.findByIdUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<CouncilMember> getMemberInCouncil(UUID councilId, UUID userId) {
        CouncilMember.CouncilMemberId id = new CouncilMember.CouncilMemberId(councilId, userId);
        return councilMemberRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean isMemberOfCouncil(UUID userId, UUID councilId) {
        CouncilMember.CouncilMemberId membershipId = new CouncilMember.CouncilMemberId(councilId, userId);
        return councilMemberRepository.existsById(membershipId);
    }

    public void joinCouncilAsBasicMember(UUID councilId, UUID userId) {
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono samorządu"));
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika"));

        CouncilMember.CouncilMemberId membershipId = new CouncilMember.CouncilMemberId(councilId, userId);
        if (councilMemberRepository.existsById(membershipId)) {
            throw ApiException.conflict("Użytkownik jest już członkiem tej rady.");
        }

        CouncilMember councilMember = new CouncilMember();
        councilMember.setId(membershipId);
        councilMember.setCouncil(council);
        councilMember.setUser(user);
        councilMember.setRole(RoleCode.CZLONEK_SU);

        councilMemberRepository.save(councilMember);
        activityLogService.log(userId, ActionType.USER_UPDATED, "Dołączono do samorządu jako członek");
    }

    public List<RoleOptionDto> getAvailableRoles() {
        return Arrays.stream(RoleCode.values())
                .filter(role -> role.getCategory() == RoleCategory.SU)
                .map(role -> new RoleOptionDto(role.name(), role.getDisplayName()))
                .collect(Collectors.toList());
    }

    private int getCouncilRank(UUID councilId, Users user) {
        RoleCode globalHighest = getHighestGlobalRole(user);
        if (globalHighest == RoleCode.ADMINISTRATOR) {
            return Integer.MAX_VALUE;
        }

        return councilMemberRepository.findByCouncilIdAndUserId(councilId, user.getId())
                .map(member -> member.getRole().getRank())
                .orElse(-1);
    }

    private RoleCode getHighestGlobalRole(Users user) {
        return user.getUserRoles().stream()
                .map(UserRole::getRole)
                .map(Role::getRoleCode)
                .max(Comparator.comparingInt(RoleCode::getRank))
                .orElse(RoleCode.UCZEN);
    }
}