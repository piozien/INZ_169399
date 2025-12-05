package pl.su.su_backend.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.su.su_backend.model.event.Event;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CalendarService {

    private final RestClient graphRestClient;

    @Value("${app.microsoft.calendar.enabled:false}")
    private boolean calendarEnabled;

    @Value("${app.microsoft.calendar.events-path.me:/me/events}")
    private String meEventsPath;

    public CalendarService(@Qualifier("graphRestClient") RestClient graphRestClient) {
        this.graphRestClient = graphRestClient;
    }

    public String createCalendarEvent(String accessToken, Event event) {
        if (!calendarEnabled) return null;

        Map<String, Object> payload = toGraphEvent(event);

        try {
            Map response = graphRestClient.post()
                    .uri(meEventsPath)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            return response != null ? String.valueOf(response.get("id")) : null;
        } catch (Exception e) {
            log.error("Failed to create calendar event: {}", e.getMessage());
            return null;
        }
    }

    public void updateCalendarEvent(String accessToken, String calendarEventId, Event event) {
        if (!calendarEnabled || calendarEventId == null) return;

        Map<String, Object> payload = toGraphEvent(event);

        try {
            graphRestClient.patch()
                    .uri(meEventsPath + "/" + calendarEventId)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to update calendar event: {}", e.getMessage());
        }
    }

    public void deleteCalendarEvent(String accessToken, String calendarEventId) {
        if (!calendarEnabled || calendarEventId == null) return;

        try {
            graphRestClient.delete()
                    .uri(meEventsPath + "/" + calendarEventId)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to delete calendar event: {}", e.getMessage());
        }
    }

    public void addAttendeeToEvent(String accessToken, String calendarEventId, String attendeeEmail) {
        if (!calendarEnabled || calendarEventId == null) return;

        Map<String, Object> attendee = Map.of(
                "emailAddress", Map.of("address", attendeeEmail, "name", attendeeEmail),
                "type", "required"
        );

        Map<String, Object> payload = Map.of("attendees", List.of(attendee));

        try {
            graphRestClient.patch()
                    .uri(meEventsPath + "/" + calendarEventId)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to add attendee: {}", e.getMessage());
        }
    }

    private Map<String, Object> toGraphEvent(Event event) {
        ZoneId polishZone = ZoneId.of("Europe/Warsaw");

        ZonedDateTime start = event.getStartDate().atZone(polishZone);
        ZonedDateTime end = event.getEndDate().atZone(polishZone);

        return Map.of(
                "subject", event.getTitle(),
                "body", Map.of(
                        "contentType", "HTML",
                        "content", event.getDescription() == null ? "" : event.getDescription()
                ),
                "start", Map.of(
                        "dateTime", start.toLocalDateTime().toString(),
                        "timeZone", polishZone.getId()
                ),
                "end", Map.of(
                        "dateTime", end.toLocalDateTime().toString(),
                        "timeZone", polishZone.getId()
                ),
                "location", Map.of(
                        "displayName", event.getLocation() == null ? "" : event.getLocation()
                )
        );
    }
}