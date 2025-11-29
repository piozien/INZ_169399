package pl.su.su_backend.service.council;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.council.CouncilMapper;
import pl.su.su_backend.dto.council.CouncilRequestDto;
import pl.su.su_backend.dto.council.CouncilResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.service.user.UserService;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CouncilService {

    private final CouncilRepository councilRepository;
    private final UserService userService;
    private final ActivityLogService activityLogService;
    private final PermissionService permissionService;
    private final CouncilMemberService councilMemberService;
    private final CouncilMapper councilMapper;


    public CouncilResponseDto createCouncil(CouncilRequestDto dto, String currentUserEmail) {
        log.info("Creating council: {} by user: {}", dto.getName(), currentUserEmail);
        Users currentUser = userService.getUserByEmailEntity(currentUserEmail);

        Council council = councilMapper.toEntity(dto);

        String joinCode = generateJoinCodeForCouncil(dto.getAcademicYear());
        council.setJoinCode(joinCode);

        council = councilRepository.save(council);

        activityLogService.log(currentUser.getId(), ActionType.COUNCIL_CREATE,
                "Created council: " + dto.getName() + " (" + joinCode + ")");

        return councilMapper.toResponseDto(council);
    }

    @Transactional(readOnly = true)
    public List<CouncilResponseDto> getCouncils(String currentUserEmail) {
        Users currentUser = userService.getUserByEmailEntity(currentUserEmail);

        boolean isAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getRoleCode() == RoleCode.ADMINISTRATOR);

        boolean canViewAll = permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_VIEW_ALL);

        if (isAdmin || canViewAll) {
            log.info("User {} has global access (Admin: {}, Perm: {}). Returning all councils.",
                    currentUserEmail, isAdmin, canViewAll);

            return councilRepository.findAll().stream()
                    .map(councilMapper::toResponseDto)
                    .collect(Collectors.toList());
        }

        log.info("User {} has restricted access. Returning memberships only.", currentUserEmail);
        List<CouncilMember> memberships = councilMemberService.getUserCouncilMemberships(currentUser.getId());

        List<UUID> councilIds = memberships.stream()
                .map(member -> member.getCouncil().getId())
                .collect(Collectors.toList());

        return councilRepository.findAllById(councilIds).stream()
                .map(councilMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouncilResponseDto getCouncilById(UUID id, String currentUserEmail) {
        Users currentUser = userService.getUserByEmailEntity(currentUserEmail);

        Council council = councilRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono samorządu o ID: " + id));

        boolean isAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getRoleCode() == RoleCode.ADMINISTRATOR);
        boolean hasGlobalAccess = permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_VIEW_ALL);
        boolean isMember = councilMemberService.isMemberOfCouncil(currentUser.getId(), id);

        if (!isAdmin && !hasGlobalAccess && !isMember) {
            throw ApiException.forbidden("Brak dostępu do tego samorządu");
        }

        return councilMapper.toResponseDto(council);
    }

    public CouncilResponseDto joinCouncilByCode(String joinCode, String currentUserEmail) {
        log.info("User {} attempting to join council with code: {}", currentUserEmail, joinCode);

        Users currentUser = userService.getUserByEmailEntity(currentUserEmail);

        Council council = councilRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono samorządu o kodzie: " + joinCode));

        if (!council.getIsActive()) {
            throw ApiException.badRequest("Ten samorząd nie jest już aktywny.");
        }

        councilMemberService.joinCouncilAsBasicMember(council.getId(), currentUser.getId());

        activityLogService.log(currentUser.getId(), ActionType.USER_UPDATED, "Joined council: " + council.getName());

        return councilMapper.toResponseDto(council);
    }

    private String generateJoinCodeForCouncil(String academicYear) {
        String yearPart = academicYear.split("/")[0];
        String prefix = "SU" + yearPart;
        String code;
        do {
            int randomNumber = new Random().nextInt(10000);
            code = prefix + String.format("%04d", randomNumber);
        } while (councilRepository.findByJoinCode(code).isPresent());
        return code;
    }
}