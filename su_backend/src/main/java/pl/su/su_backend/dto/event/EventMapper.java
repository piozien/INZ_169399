package pl.su.su_backend.dto.event;

import pl.su.su_backend.model.event.Event;
import pl.su.su_backend.model.event.EventParticipant;

import java.util.List;
import java.util.stream.Collectors;

public class EventMapper {

	private EventMapper() {}

	public static EventResponseDto toResponse(Event event) {
		if (event == null) return null;
		
		List<ParticipantResponseDto> participants = event.getParticipants() != null 
			? event.getParticipants().stream()
				.map(EventMapper::toResponse)
				.collect(Collectors.toList())
			: List.of();
		
		return EventResponseDto.builder()
				.id(event.getId())
				.title(event.getTitle())
				.description(event.getDescription())
				.startDate(event.getStartDate())
				.endDate(event.getEndDate())
				.location(event.getLocation())
				.createdById(event.getCreatedBy() != null ? event.getCreatedBy().getId() : null)
				.calendarEventId(event.getCalendarEventId())
				.createdAt(event.getCreatedAt())
				.status(event.getStatus())
				.participants(participants)
				.build();
	}

	public static ParticipantResponseDto toResponse(EventParticipant ep) {
		if (ep == null) return null;
		return ParticipantResponseDto.builder()
				.eventId(ep.getEvent() != null ? ep.getEvent().getId() : null)
				.userId(ep.getUser() != null ? ep.getUser().getId() : null)
				.role(ep.getRole())
				.confirmed(ep.getConfirmed())
				.assignedAt(ep.getAssignedAt())
				.build();
	}
}


