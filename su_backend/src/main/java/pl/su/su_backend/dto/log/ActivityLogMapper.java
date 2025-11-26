package pl.su.su_backend.dto.log;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.su.su_backend.model.log.ActivityLog;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {

    @Mapping(target = "userId", source = "user.id")
    ActivityLogResponseDto toResponse(ActivityLog activityLog);
}