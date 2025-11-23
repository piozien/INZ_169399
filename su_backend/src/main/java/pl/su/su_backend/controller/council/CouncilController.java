package pl.su.su_backend.controller.council;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.council.CouncilMemberDto;
import pl.su.su_backend.dto.council.CouncilMemberMapper;
import pl.su.su_backend.dto.council.CouncilRequestDto;
import pl.su.su_backend.dto.council.CouncilResponseDto;
import pl.su.su_backend.model.council.CouncilMember;
import pl.su.su_backend.service.auth.AuthenticationService;
import pl.su.su_backend.service.council.CouncilMemberService;
import pl.su.su_backend.service.council.CouncilService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/council")
@RequiredArgsConstructor
@Slf4j
public class CouncilController {

    private final CouncilService councilService;
    private final CouncilMemberService councilMemberService;
    private final AuthenticationService authenticationService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'COUNCIL_CREATE')")
    public ResponseEntity<CouncilResponseDto> createCouncil(@Valid @RequestBody CouncilRequestDto dto,
                                                            @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Creating council: {} for academic year: {} by user: {}", dto.getName(), dto.getAcademicYear(), email);
        CouncilResponseDto council = councilService.createCouncil(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(council);
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<List<CouncilResponseDto>> getCouncils(@AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching councils for user: {}", email);
        List<CouncilResponseDto> councils = councilService.getCouncils(email);
        return ResponseEntity.ok(councils);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<CouncilResponseDto> getCouncilById(@PathVariable UUID id,
                                                             @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching council by ID: {} for user: {}", id, email);
        CouncilResponseDto council = councilService.getCouncilById(id, email);
        return ResponseEntity.ok(council);
    }

    @PostMapping("/{councilId}/members/{userId}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_MEMBER_MANAGE')")
    public ResponseEntity<CouncilResponseDto> addMemberToCouncil(@PathVariable UUID councilId,
                                                                 @PathVariable UUID userId,
                                                                 @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Adding member {} to council {} by user: {}", userId, councilId, email);
        CouncilResponseDto council = councilService.addMemberToCouncil(councilId, userId, email);
        return ResponseEntity.ok(council);
    }

    @DeleteMapping("/{councilId}/members/{userId}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_MEMBER_MANAGE')")
    public ResponseEntity<CouncilResponseDto> removeMemberFromCouncil(@PathVariable UUID councilId,
                                                                      @PathVariable UUID userId,
                                                                      @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Removing member {} from council {} by user: {}", userId, councilId, email);
        CouncilResponseDto council = councilService.removeMemberFromCouncil(councilId, userId, email);
        return ResponseEntity.ok(council);
    }

    @GetMapping("/{councilId}/members")
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<List<CouncilMemberDto>> getCouncilMembers(@PathVariable UUID councilId,
                                                                     @AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("Fetching members of council {} by user: {}", councilId, email);
        List<CouncilMember> members = councilMemberService.getCouncilMembers(councilId, email);
        List<CouncilMemberDto> memberDtos = members.stream()
                .map(CouncilMemberMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(memberDtos);
    }

    @PostMapping("/join/{joinCode}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_JOIN')")
    public ResponseEntity<CouncilResponseDto> joinCouncilByCode(
            @PathVariable String joinCode,
            @AuthenticationPrincipal Object principal) {

        String email = authenticationService.getEmailFromPrincipal(principal);
        log.info("User {} attempting to join council with code: {}", email, joinCode);

        CouncilResponseDto response = councilService.joinCouncilByCode(joinCode, email);

        return ResponseEntity.ok(response);
    }

}
