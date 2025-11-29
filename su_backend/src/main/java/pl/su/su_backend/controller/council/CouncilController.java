package pl.su.su_backend.controller.council;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.su.su_backend.dto.council.CouncilRequestDto;
import pl.su.su_backend.dto.council.CouncilResponseDto;
import pl.su.su_backend.dto.council.RoleOptionDto;
import pl.su.su_backend.service.council.CouncilMemberService;
import pl.su.su_backend.service.council.CouncilService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/councils")
@RequiredArgsConstructor
@Slf4j
public class CouncilController {

    private final CouncilService councilService;
    private final CouncilMemberService councilMemberService;

    @PostMapping
    @PreAuthorize("hasPermission(null, 'COUNCIL_CREATE')")
    public ResponseEntity<CouncilResponseDto> createCouncil(
            @Valid @RequestBody CouncilRequestDto dto,
            @AuthenticationPrincipal Object principal) {
        String email = getCurrentUserEmail(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(councilService.createCouncil(dto, email));
    }

    @GetMapping
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<List<CouncilResponseDto>> getCouncils(@AuthenticationPrincipal Object principal) {
        String email = getCurrentUserEmail(principal);
        return ResponseEntity.ok(councilService.getCouncils(email));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'COUNCIL_VIEW')")
    public ResponseEntity<CouncilResponseDto> getCouncilById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Object principal) {
        String email = getCurrentUserEmail(principal);
        return ResponseEntity.ok(councilService.getCouncilById(id, email));
    }
    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleOptionDto>> getAvailableRoles() {
        return ResponseEntity.ok(councilMemberService.getAvailableRoles());
    }

    @PostMapping("/join/{joinCode}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CouncilResponseDto> joinCouncilByCode(
            @PathVariable String joinCode,
            @AuthenticationPrincipal Object principal) {
        String email = getCurrentUserEmail(principal);
        return ResponseEntity.ok(councilService.joinCouncilByCode(joinCode, email));
    }

    private String getCurrentUserEmail(Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }
}