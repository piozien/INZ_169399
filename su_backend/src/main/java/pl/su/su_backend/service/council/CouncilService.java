package pl.su.su_backend.service.council;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.council.CouncilContextDto;
import pl.su.su_backend.dto.council.CouncilMapper;
import pl.su.su_backend.dto.council.CouncilRequestDto;
import pl.su.su_backend.dto.council.CouncilResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.council.Council;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.enums.PermissionCode;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.model.users.UserRole;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.council.CouncilRepository;
import pl.su.su_backend.repositories.role.RoleRepository;
import pl.su.su_backend.service.auth.PermissionService;
import pl.su.su_backend.service.log.ActivityLogService;
import pl.su.su_backend.service.user.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
    private final RoleRepository roleRepository;

    public CouncilResponseDto createCouncil(CouncilRequestDto dto, String currentUserEmail) {
        log.info("Creating council: {} by user: {}", dto.getName(), currentUserEmail);
        Users currentUser = userService.getUserByEmailEntity(currentUserEmail);

        if (!permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_CREATE)) {
            boolean isAdmin = currentUser.getUserRoles().stream()
                    .anyMatch(ur -> RoleCode.ADMINISTRATOR.equals(ur.getRole().getRoleCode()));
            if (!isAdmin) {
                throw ApiException.forbidden("Brak uprawnień do tworzenia samorządów.");
            }
        }

        Council council = councilMapper.toEntity(dto);
        String joinCode = generateJoinCodeForCouncil(dto.getAcademicYear());
        council.setJoinCode(joinCode);

        if (council.isDefaultCouncil()) {
            disablePreviousDefault(null);
            council.setActive(true);
        }

        council = councilRepository.save(council);
        activityLogService.log(currentUser.getId(), ActionType.COUNCIL_CREATE, "Utworzono samorząd: " + dto.getName());
        return councilMapper.toResponseDto(council);
    }

    public CouncilResponseDto updateCouncil(UUID councilId, CouncilRequestDto dto, String currentUserEmail) {
        Users currentUser = userService.getUserByEmailEntity(currentUserEmail);
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono samorządu"));

        boolean isAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> RoleCode.ADMINISTRATOR.equals(ur.getRole().getRoleCode()));
        boolean hasLocalPerm = permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_EDIT, councilId);

        if (!isAdmin && !hasLocalPerm) {
            throw ApiException.forbidden("Brak uprawnień do edycji tego samorządu.");
        }

        if (dto.isDefaultCouncil()) {
            disablePreviousDefault(councilId);

            council.setDefaultCouncil(true);
            council.setActive(true);
        } else {
            council.setDefaultCouncil(false);
        }

        if (!dto.isDefaultCouncil()) {
            council.setActive(dto.isActive());
            if (!council.isActive()) {
                council.setDefaultCouncil(false);
            }
        }

        if (dto.getName() != null) council.setName(dto.getName());
        if (dto.getAcademicYear() != null) council.setAcademicYear(dto.getAcademicYear());
        if (dto.getStartDate() != null) council.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) council.setEndDate(dto.getEndDate());

        Council saved = councilRepository.save(council);
        activityLogService.log(currentUser.getId(), ActionType.COUNCIL_UPDATE, "Zaktualizowano samorząd: " + saved.getName());

        return councilMapper.toResponseDto(saved);
    }


    public void deleteCouncil(UUID councilId, String currentUserEmail) {
        Users currentUser = userService.getUserByEmailEntity(currentUserEmail);
        Council council = councilRepository.findById(councilId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono samorządu"));

        boolean isAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> RoleCode.ADMINISTRATOR.equals(ur.getRole().getRoleCode()));

        boolean hasDeletePerm = permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_DELETE);

        if (!isAdmin && !hasDeletePerm) {
            throw ApiException.forbidden("Tylko Administrator może usunąć samorząd.");
        }

        councilRepository.delete(council);
        activityLogService.log(currentUser.getId(), ActionType.COUNCIL_DELETE, "Usunięto samorząd: " + council.getName());
    }

    @Transactional(readOnly = true)
    public List<CouncilResponseDto> getCouncils(String currentUserEmail) {
        Users currentUser = userService.getUserByEmailEntity(currentUserEmail);

        boolean isAdmin = currentUser.getUserRoles().stream()
                .map(UserRole::getRole)
                .anyMatch(role -> RoleCode.ADMINISTRATOR.equals(role.getRoleCode()));

        if (isAdmin) {
            return councilRepository.findAll().stream()
                    .map(councilMapper::toResponseDto)
                    .collect(Collectors.toList());
        }

        if (permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_VIEW_ALL)) {
            return councilRepository.findAll().stream()
                    .map(councilMapper::toResponseDto)
                    .collect(Collectors.toList());
        }

        List<CouncilMember> memberships = councilMemberService.getUserCouncilMemberships(currentUser.getId());

        List<UUID> councilIds = memberships.stream()
                .map(member -> member.getCouncil().getId())
                .collect(Collectors.toList());

        return councilRepository.findAllById(councilIds).stream()
                .map(councilMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouncilContextDto getUserContextInCouncil(UUID councilId, String userEmail) {
        Users user = userService.getUserByEmailEntity(userEmail);

        boolean isAdmin = user.getUserRoles().stream()
                .anyMatch(ur -> RoleCode.ADMINISTRATOR.equals(ur.getRole().getRoleCode()));

        Set<String> permissions = new HashSet<>();
        String roleName = null;
        boolean isMember = false;

        if (isAdmin) {
            permissions.add("ALL_ACCESS");
            roleName = "ADMINISTRATOR";
            isMember = true;
        } else {
            var memberOpt = councilMemberService.getMemberInCouncil(councilId, user.getId());
            if (memberOpt.isPresent()) {
                isMember = true;
                RoleCode code = memberOpt.get().getRole();
                roleName = code.name();

                roleRepository.findByRoleCode(code).ifPresent(roleEntity -> {
                    roleEntity.getPermissions().forEach(p -> permissions.add(p.getName()));
                });
            }
        }

        return CouncilContextDto.builder()
                .isMember(isMember)
                .role(roleName)
                .permissions(permissions)
                .build();
    }

    @Transactional(readOnly = true)
    public CouncilResponseDto getCouncilById(UUID id, String currentUserEmail) {
        Users currentUser = userService.getUserByEmailEntity(currentUserEmail);

        Council council = councilRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono samorządu o ID: " + id));

        CouncilResponseDto dto = councilMapper.toResponseDto(council);

        Set<String> permissions = new HashSet<>();

        boolean isAdmin = currentUser.getUserRoles().stream()
                .anyMatch(ur -> RoleCode.ADMINISTRATOR.equals(ur.getRole().getRoleCode()));

        if (isAdmin) {
            permissions.add("ALL_ACCESS");
        } else {
            councilMemberService.getMemberInCouncil(id, currentUser.getId())
                    .ifPresent(member -> {
                        RoleCode code = member.getRole();
                        permissions.add("ROLE_" + code.name());
                        roleRepository.findByRoleCode(code).ifPresent(roleEntity -> {
                            roleEntity.getPermissions().forEach(p -> permissions.add(p.getName()));
                        });
                    });
        }

        dto.setMyPermissions(permissions);

        boolean hasGlobalAccess = permissionService.hasPermission(currentUser.getId(), PermissionCode.COUNCIL_VIEW_ALL);
        boolean isMember = !permissions.isEmpty();

        if (!isAdmin && !hasGlobalAccess && !isMember) {
            throw ApiException.forbidden("Brak dostępu do tego samorządu");
        }

        return dto;
    }

    public CouncilResponseDto joinCouncilByCode(String joinCode, String currentUserEmail) {
        Users currentUser = userService.getUserByEmailEntity(currentUserEmail);

        Council council = councilRepository.findByJoinCode(joinCode)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono samorządu o kodzie: " + joinCode));

        if (!council.isActive()) {
            throw ApiException.badRequest("Ten samorząd nie jest już aktywny.");
        }

        councilMemberService.joinCouncilAsBasicMember(council.getId(), currentUser.getId());

        activityLogService.log(currentUser.getId(), ActionType.USER_UPDATED, "Dołączył do samorządu: " + council.getName());

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

    private void disablePreviousDefault(UUID currentCouncilId) {
        councilRepository.findFirstByActiveTrueAndDefaultCouncilTrue()
                .ifPresent(currentDefault -> {
                    if (!currentDefault.getId().equals(currentCouncilId)) {
                        currentDefault.setDefaultCouncil(false);
                        councilRepository.save(currentDefault);
                    }
                });
    }
}