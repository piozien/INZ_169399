package pl.su.su_backend.controller.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.su.su_backend.dto.dashboard.DashboardSummaryResponseDto;
import pl.su.su_backend.dto.dashboard.UserProfileDataDto;
import pl.su.su_backend.service.auth.AuthenticationService;
import pl.su.su_backend.service.dashboard.DashboardService;
import pl.su.su_backend.service.user.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponseDto> getSummary(@AuthenticationPrincipal Object principal) {
        String email = authenticationService.getEmailFromPrincipal(principal);
        UUID userId = userService.getCurrentUserId(email);
        return ResponseEntity.ok(dashboardService.getDashboardSummary(userId));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfileDataDto> getUserProfileData(@PathVariable UUID userId) {
        return ResponseEntity.ok(dashboardService.getUserProfileData(userId));
    }
}
