package pl.su.su_backend.controller.council;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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

    @GetMapping
    public ResponseEntity<List<CouncilMemberDto>> getCouncilMembers(
            @PathVariable UUID councilId,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userEmail = jwt.getClaimAsString("sub");
        log.info("Getting members of council {} by user: {}", councilId, userEmail);

        List<CouncilMember> members = councilMemberService.getCouncilMembers(councilId, userEmail);
        List<CouncilMemberDto> memberDto = members.stream()
                .map(CouncilMemberMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(memberDto);
    }

    @PostMapping
    public ResponseEntity<CouncilMemberDto> addMemberToCouncil(
            @PathVariable UUID councilId,
            @RequestParam UUID userId,
            @RequestParam RoleCode roleCode,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userEmail = jwt.getClaimAsString("sub");
        log.info("Adding member {} to council {} with role {} by user: {}", userId, councilId, roleCode, userEmail);

        CouncilMember member = councilMemberService.addMemberWithRole(councilId, userId, roleCode, userEmail);
        CouncilMemberDto memberDto = CouncilMemberMapper.toDto(member);

        return ResponseEntity.ok(memberDto);
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<CouncilMemberDto> updateMemberRole(
            @PathVariable UUID councilId,
            @PathVariable UUID userId,
            @RequestParam RoleCode roleCode,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userEmail = jwt.getClaimAsString("sub");
        log.info("Updating member {} role to {} in council {} by user: {}", userId, roleCode, councilId, userEmail);

        CouncilMember member = councilMemberService.updateMemberRole(councilId, userId, roleCode, userEmail);
        CouncilMemberDto memberDto = CouncilMemberMapper.toDto(member);

        return ResponseEntity.ok(memberDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMemberFromCouncil(
            @PathVariable UUID councilId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal Jwt jwt) {
        
        String userEmail = jwt.getClaimAsString("sub");
        log.info("Removing member {} from council {} by user: {}", userId, councilId, userEmail);

        councilMemberService.removeMember(councilId, userId, userEmail);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-memberships")
    public ResponseEntity<List<CouncilMemberDto>> getMyCouncilMemberships(
            @AuthenticationPrincipal Jwt jwt) {
        
        String userEmail = jwt.getClaimAsString("sub");
        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));
        log.info("Getting council memberships for user: {}", userEmail);

        List<CouncilMember> memberships = councilMemberService.getUserCouncilMemberships(userId);
        List<CouncilMemberDto> membershipDto = memberships.stream()
                .map(CouncilMemberMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(membershipDto);
    }
}

