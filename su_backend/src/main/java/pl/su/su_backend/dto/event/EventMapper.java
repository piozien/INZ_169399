package pl.su.su_backend.dto.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.su.su_backend.model.event.Event;
import pl.su.su_backend.model.event.EventParticipant;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "participantsCount", ignore = true)
    @Mapping(target = "council", ignore = true)
    Event toEntity(EventRequestDto dto);

    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "councilId", source = "council.id")
    EventResponseDto toResponse(Event event);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFullName", source = "user.fullName")
    @Mapping(target = "userEmail", source = "user.email")
    ParticipantResponseDto toParticipantResponse(EventParticipant participant);
}