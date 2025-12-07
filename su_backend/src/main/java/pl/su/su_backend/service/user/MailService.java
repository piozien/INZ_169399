package pl.su.su_backend.service.user;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.su.su_backend.model.event.Event;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final EmailClient emailClient;

    @Value("${ACS_SENDER_ADDRESS}")
    private String senderAddress;

    private static final ZoneId POLAND_ZONE = ZoneId.of("Europe/Warsaw");

    private static final DateTimeFormatter GOOGLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter OUTLOOK_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Async
    public void sendEventInvitation(String toEmail, String fullName, Event event) {
        log.info("Azure Email: Wysyłanie linków do kalendarza do {}", toEmail);

        String title = event.getTitle();
        String location = event.getLocation() != null ? event.getLocation() : "Online";

        ZonedDateTime startPl = event.getStartDate().atZone(POLAND_ZONE);
        ZonedDateTime endPl = event.getEndDate().atZone(POLAND_ZONE);

        String googleStartUtc = GOOGLE_DATE_FORMAT.format(startPl.withZoneSameInstant(ZoneId.of("UTC")));
        String googleEndUtc = GOOGLE_DATE_FORMAT.format(endPl.withZoneSameInstant(ZoneId.of("UTC")));

        String outlookStart = OUTLOOK_DATE_FORMAT.format(event.getStartDate());
        String outlookEnd = OUTLOOK_DATE_FORMAT.format(event.getEndDate());

        String googleLink = generateGoogleCalendarLink(title, googleStartUtc, googleEndUtc, event.getDescription(), location);
        String outlookWebLink = generateOutlookWebLink(title, outlookStart, outlookEnd, event.getDescription(), location);
        String office365Link = generateOffice365Link(title, outlookStart, outlookEnd, event.getDescription(), location);

        String subject = "Zaproszenie: " + title;

        String btnStyleBase = "padding: 12px 24px; text-align: center; text-decoration: none; display: inline-block; font-size: 14px; margin: 5px 2px; cursor: pointer; border-radius: 6px; font-weight: bold; font-family: sans-serif;";
        String btnPrimary = btnStyleBase + "background-color: #0078D4; border: 1px solid #0078D4; color: white;";
        String btnSecondary = btnStyleBase + "background-color: #f3f4f6; border: 1px solid #d1d5db; color: #1f2937;";

        String htmlContent = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 20px; max-width: 600px; color: #333;">
                    <h2 style="color: #0078D4;">Cześć %s!</h2>
                    <p>Potwierdzamy Twój udział w wydarzeniu: <strong style="font-size: 1.1em;">%s</strong>.</p>
                    
                    <div style="background-color: #f0f9ff; padding: 20px; border-radius: 8px; border-left: 5px solid #0078D4; margin: 25px 0;">
                        <p style="margin: 5px 0;"><strong>Kiedy (Polska):</strong> %s - %s</p>
                        <p style="margin: 5px 0;"><strong>Gdzie:</strong> %s</p>
                    </div>

                    <p style="margin-bottom: 15px;">Kliknij poniżej, aby dodać wydarzenie do swojego kalendarza:</p>
                    
                    <div style="margin-top: 10px;">
                        <a href="%s" style="%s">Outlook (Szkolny / Office 365)</a>
                    </div>
                    <div style="margin-top: 10px;">
                        <a href="%s" style="%s">Outlook (Prywatny)</a>
                        <a href="%s" style="%s">Google Calendar</a>
                    </div>

                    <hr style="margin: 40px 0; border: 0; border-top: 1px solid #eee;" />
                    <p style="font-size: 12px; color: #888;">Wiadomość wygenerowana automatycznie przez System Samorządu Uczniowskiego.</p>
                </div>
                """.formatted(
                fullName,
                title,
                event.getStartDate().toLocalTime(), event.getEndDate().toLocalTime(),
                location,
                office365Link, btnPrimary,
                outlookWebLink, btnSecondary,
                googleLink, btnSecondary
        );

        sendEmail(toEmail, subject, htmlContent);
    }


    private String generateGoogleCalendarLink(String title, String startUtc, String endUtc, String description, String location) {
        return "https://www.google.com/calendar/render?action=TEMPLATE" +
                "&text=" + encode(title) +
                "&dates=" + startUtc + "/" + endUtc +
                "&details=" + encode(description) +
                "&location=" + encode(location) +
                "&ctz=" + encode(POLAND_ZONE.getId());
    }

    private String generateOutlookWebLink(String title, String start, String end, String description, String location) {
        return "https://outlook.live.com/calendar/0/deeplink/compose?path=/calendar/action/compose&rru=addevent" +
                "&startdt=" + start +
                "&enddt=" + end +
                "&subject=" + encode(title) +
                "&body=" + encode(description) +
                "&location=" + encode(location);
    }

    private String generateOffice365Link(String title, String start, String end, String description, String location) {
        return "https://outlook.office.com/calendar/0/deeplink/compose?path=/calendar/action/compose&rru=addevent" +
                "&startdt=" + start +
                "&enddt=" + end +
                "&subject=" + encode(title) +
                "&body=" + encode(description) +
                "&location=" + encode(location);
    }

    private String encode(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
    @Async
    public void sendActivationEmail(String toEmail, String fullName, String activationUrl) {
        String subject = "Aktywacja konta - System Samorządu";
        String htmlContent = "<h2>Cześć " + fullName + "!</h2><p><a href='" + activationUrl + "'>Aktywuj konto</a></p>";
        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        String subject = "Witaj w Systemie Samorządu!";
        String htmlContent = "<h2>Witaj " + fullName + "!</h2><p>Twoje konto jest aktywne.</p>";
        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetUrl) {
        String subject = "Reset hasła";
        String htmlContent = "<a href='" + resetUrl + "'>Zresetuj hasło</a>";
        sendEmail(toEmail, subject, htmlContent);
    }

    private void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            EmailMessage message = new EmailMessage()
                    .setSenderAddress(senderAddress)
                    .setToRecipients(toEmail)
                    .setSubject(subject)
                    .setBodyHtml(htmlContent);

            SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
            poller.waitForCompletion();
        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage());
        }
    }
}
