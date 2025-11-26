package pl.su.su_backend.service.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.su.su_backend.dto.log.ActivityLogMapper;
import pl.su.su_backend.dto.log.ActivityLogResponseDto;
import pl.su.su_backend.exception.ApiException;
import pl.su.su_backend.model.enums.ActionType;
import pl.su.su_backend.model.log.ActivityLog;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.repositories.log.ActivityLogRepository;
import pl.su.su_backend.repositories.user.UsersRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UsersRepository usersRepository;
    private final ActivityLogMapper activityLogMapper;

    @Transactional
    public void log(UUID actingUserId, ActionType actionType, String action) {
        Users user = usersRepository.findById(actingUserId)
                .orElseThrow(() -> ApiException.notFound("Nie znaleziono użytkownika do logowania akcji"));

        ActivityLog logEntry = ActivityLog.builder()
                .user(user)
                .actionType(actionType)
                .action(action)
                .createdAt(LocalDateTime.now())
                .build();

        activityLogRepository.save(logEntry);
    }


    @Transactional(readOnly = true)
    public List<ActivityLogResponseDto> listForUser(UUID userId) {
        if (!usersRepository.existsById(userId)) {
            throw ApiException.notFound("Użytkownik nie istnieje");
        }

        return activityLogRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(activityLogMapper::toResponse)
                .collect(Collectors.toList());
    }
}