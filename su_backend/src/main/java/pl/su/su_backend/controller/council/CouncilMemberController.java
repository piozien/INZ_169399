package pl.su.su_backend.controller.council;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.council.CouncilMemberDto;
import pl.su.su_backend.dto.council.CouncilMemberMapper;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.model.enums.RoleCode;
import pl.su.su_backend.service.council.CouncilMemberService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/councils/{councilId}/members")
@RequiredArgsConstructor
@Slf4j
public class CouncilMemberController {

    private final CouncilMemberService councilMemberService;
    private final CouncilMemberMapper councilMemberMapper;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<List<CouncilMemberDto>> getCouncilMembers(
            @PathVariable UUID councilId,
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        List<CouncilMember> members = councilMemberService.getCouncilMembers(councilId, email);

        return ResponseEntity.ok(members.stream()
                .map(councilMemberMapper::toDto)
                .collect(Collectors.toList()));
    }


    @PostMapping
    @PreAuthorize("hasPermission(null, 'COUNCIL_MEMBER_MANAGE')")
    public ResponseEntity<CouncilMemberDto> addMemberToCouncil(
            @PathVariable UUID councilId,
            @RequestParam UUID userId,
            @RequestParam RoleCode roleCode,
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        CouncilMember member = councilMemberService.addMemberWithRole(councilId, userId, roleCode, email);

        return ResponseEntity.ok(councilMemberMapper.toDto(member));
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasPermission(null, 'COUNCIL_MEMBER_MANAGE')")
    public ResponseEntity<CouncilMemberDto> updateMemberRole(
            @PathVariable UUID councilId,
            @PathVariable UUID userId,
            @RequestParam RoleCode roleCode,
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        CouncilMember member = councilMemberService.updateMemberRole(councilId, userId, roleCode, email);

        return ResponseEntity.ok(councilMemberMapper.toDto(member));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeMemberFromCouncil(
            @PathVariable UUID councilId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal Object principal) {

        String email = getCurrentUserEmail(principal);
        councilMemberService.removeMember(councilId, userId, email);

        return ResponseEntity.noContent().build();
    }

    private String getCurrentUserEmail(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }
}