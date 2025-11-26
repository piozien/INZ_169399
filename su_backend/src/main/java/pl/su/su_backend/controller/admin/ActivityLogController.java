package pl.su.su_backend.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.su.su_backend.dto.log.ActivityLogResponseDto;
import pl.su.su_backend.service.log.ActivityLogService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Slf4j
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasPermission(null, 'ACTIVITY_LOG_VIEW')")
    public ResponseEntity<List<ActivityLogResponseDto>> listForUser(@PathVariable UUID userId) {
        log.info("Downloading activity logs for user ID: {}", userId);
        return ResponseEntity.ok(activityLogService.listForUser(userId));
    }
}