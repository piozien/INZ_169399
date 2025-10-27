// https://learn.microsoft.com/en-us/graph/api/resources/event?view=graph-rest-1.0 24.10 - 25.10 - 13:30
package pl.su.su_backend.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.su.su_backend.model.event.Event;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final WebClient graphWebClient;

    @Value("${app.microsoft.calendar.enabled:false}")
    private boolean calendarEnabled;

    @Value("${app.microsoft.calendar.use-shared-owner}")
    private boolean useSharedOwner;

    @Value("${app.microsoft.calendar.owner-user-id}")
    private String ownerUserId;

    @Value("${app.microsoft.calendar.events-path.me}")
    private String meEventsPath;

    @Value("${app.microsoft.calendar.events-path.user}")
    private String userEventsPath;

    public String createCalendarEvent(String accessToken, Event event) {
        if (!calendarEnabled) return null;
        String path = resolveEventsPath();
        Map<String, Object> payload = toGraphEvent(event);
        return graphWebClient.post()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .map(body -> String.valueOf(body.get("id")))
                .block();
    }

    public void updateCalendarEvent(String accessToken, String calendarEventId, Event event) {
        if (!calendarEnabled || calendarEventId == null) return;
        String path = resolveEventsPath() + "/" + calendarEventId;
        Map<String, Object> payload = toGraphEvent(event);
        graphWebClient.patch()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void deleteCalendarEvent(String accessToken, String calendarEventId) {
        if (!calendarEnabled || calendarEventId == null) return;
        String path = resolveEventsPath() + "/" + calendarEventId;
        graphWebClient.delete()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
    
    public void addAttendeeToEvent(String accessToken, String calendarEventId, String attendeeEmail) {
        if (!calendarEnabled || calendarEventId == null) return;
        
        String path = resolveEventsPath() + "/" + calendarEventId;
        
        Map<String, Object> attendee = Map.of(
            "emailAddress", Map.of(
                "address", attendeeEmail,
                "name", attendeeEmail
            ),
            "type", "required"
        );
        
        Map<String, Object> payload = Map.of(
            "attendees", List.of(attendee)
        );
        
        graphWebClient.patch()
                .uri(path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private String resolveEventsPath() {
        if (useSharedOwner && ownerUserId != null && !ownerUserId.isBlank()) {
            return userEventsPath.replace("{userId}", ownerUserId);
        }
        return meEventsPath;
    }

    private Map<String, Object> toGraphEvent(Event event) {
        OffsetDateTime start = event.getStartDate().atOffset(ZoneOffset.systemDefault().getRules().getOffset(event.getStartDate()));
        OffsetDateTime end = event.getEndDate().atOffset(ZoneOffset.systemDefault().getRules().getOffset(event.getEndDate()));
        return Map.of(
                "subject", event.getTitle(),
                "body", Map.of(
                        "contentType", "HTML",
                        "content", event.getDescription() == null ? "" : event.getDescription()
                ),
                "start", Map.of(
                        "dateTime", start.toString(),
                        "timeZone", start.getOffset().toString()
                ),
                "end", Map.of(
                        "dateTime", end.toString(),
                        "timeZone", end.getOffset().toString()
                ),
                "location", Map.of(
                        "displayName", event.getLocation() == null ? "" : event.getLocation()
                )
        );
    }
}


