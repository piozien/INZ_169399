package pl.su.su_backend.dto.log;

import pl.su.su_backend.model.log.ActivityLog;

public class ActivityLogMapper {

	private ActivityLogMapper() {}

	public static ActivityLogResponseDto toResponse(ActivityLog log) {
		if (log == null) return null;
		return ActivityLogResponseDto.builder()
				.id(log.getId())
				.userId(log.getUser() != null ? log.getUser().getId() : null)
				.actionType(log.getActionType())
				.action(log.getAction())
				.createdAt(log.getCreatedAt())
				.build();
	}
}


