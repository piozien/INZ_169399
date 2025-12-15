package pl.su.su_backend.service.user;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.SyncPoller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.su.su_backend.model.event.Event;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    @Value("$FRONTEND_URL{}")
    private String frontendUrl;

    private static final String COLOR_PRIMARY = "#c2410c";
    private static final String COLOR_BG = "#f8fafc";
    private static final String COLOR_TEXT = "#0f172a";
    private static final String COLOR_GRAY = "#64748b";

    private static final ZoneId POLAND_ZONE = ZoneId.of("Europe/Warsaw");
    private static final DateTimeFormatter GOOGLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter OUTLOOK_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private String buildEmailTemplate(String headerText, String messageHtml, String buttonText, String buttonUrl) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html><body style='font-family: Arial, sans-serif; background-color: ").append(COLOR_BG).append("; margin: 0; padding: 0;'>");

        html.append("<table width='100%' border='0' cellspacing='0' cellpadding='0'><tr><td align='center' style='padding: 40px 20px;'>");

        html.append("<table width='600' border='0' cellspacing='0' cellpadding='0' style='background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); overflow: hidden;'>");

        html.append("<tr><td style='background-color: ").append(COLOR_PRIMARY).append("; height: 6px;'></td></tr>");

        html.append("<tr><td style='padding: 40px;'>");
        html.append("<h1 style='color: ").append(COLOR_TEXT).append("; font-size: 24px; margin-bottom: 20px; margin-top: 0;'>").append(headerText).append("</h1>");
        html.append("<div style='color: #334155; font-size: 16px; line-height: 1.6;'>").append(messageHtml).append("</div>");

        if (buttonText != null && buttonUrl != null) {
            html.append("<div style='margin-top: 30px;'>");
            html.append("<a href='").append(buttonUrl).append("' style='background-color: ").append(COLOR_PRIMARY)
                    .append("; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;'>")
                    .append(buttonText).append("</a>");
            html.append("</div>");
            html.append("<p style='margin-top: 20px; font-size: 12px; color: ").append(COLOR_GRAY).append(";'>")
                    .append("Jeśli przycisk nie działa, skopiuj ten link: <br><a href='").append(buttonUrl).append("' style='color: ").append(COLOR_PRIMARY).append(";'>")
                    .append(buttonUrl).append("</a></p>");
        }

        html.append("</td></tr>");

        html.append("<tr><td style='background-color: #f1f5f9; padding: 20px; text-align: center; color: ").append(COLOR_GRAY).append("; font-size: 12px;'>");
        html.append("&copy; ").append(java.time.Year.now().getValue()).append(" System Samorządu Uczniowskiego.<br>Wiadomość wygenerowana automatycznie.");
        html.append("</td></tr>");

        html.append("</table>");
        html.append("</td></tr></table>");
        html.append("</body></html>");

        return html.toString();
    }


    @Async
    public void sendActivationEmail(String toEmail, String fullName, String activationUrl) {
        String subject = "Aktywacja konta - System Samorządu";
        String message = "<p>Cześć <b>" + fullName + "</b>!</p>" +
                "<p>Dziękujemy za rejestrację. Aby korzystać z systemu, musisz potwierdzić swój adres e-mail.</p>" +
                "<p>Link jest ważny przez 24 godziny.</p>";

        String htmlContent = buildEmailTemplate("Witaj w Samorządzie!", message, "Aktywuj Konto", activationUrl);
        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        String subject = "Witaj w Systemie Samorządu!";
        String message = "<p>Cześć <b>" + fullName + "</b>!</p>" +
                "<p>Twoje konto zostało pomyślnie aktywowane.</p>" +
                "<p>Możesz teraz zalogować się do panelu i korzystać z funkcji systemu.</p>";

        String htmlContent = buildEmailTemplate("Konto Aktywne", message, "Przejdź do systemu", frontendUrl);
        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetUrl) {
        String subject = "Reset hasła";
        String message = "<p>Cześć <b>" + fullName + "</b>,</p>" +
                "<p>Otrzymaliśmy prośbę o zresetowanie hasła do Twojego konta.</p>" +
                "<p>Jeśli to nie Ty, zignoruj tę wiadomość. Link wygaśnie za 60 minut.</p>";

        String htmlContent = buildEmailTemplate("Reset Hasła", message, "Zresetuj Hasło", resetUrl);
        sendEmail(toEmail, subject, htmlContent);
    }


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

        String btnPrimary = btnStyleBase + "background-color: " + COLOR_PRIMARY + "; border: 1px solid " + COLOR_PRIMARY + "; color: white;";

        String btnSecondary = btnStyleBase + "background-color: #f1f5f9; border: 1px solid #cbd5e1; color: #334155;";

        String htmlContent = """
                <!DOCTYPE html>
                <html><body style="font-family: Arial, sans-serif; background-color: %s; margin: 0; padding: 0;">
                <table width="100%%" border="0" cellspacing="0" cellpadding="0"><tr><td align="center" style="padding: 40px 20px;">
                    <table width="600" border="0" cellspacing="0" cellpadding="0" style="background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.05); overflow: hidden;">
                        <tr><td style="background-color: %s; height: 6px;"></td></tr>
                        <tr><td style="padding: 40px;">
                            <h2 style="color: %s; margin-top: 0;">Cześć %s!</h2>
                            <p style="color: #334155;">Potwierdzamy Twój udział w wydarzeniu: <strong style="color: #000;">%s</strong>.</p>
                            
                            <div style="background-color: #f8fafc; padding: 20px; border-radius: 8px; border-left: 5px solid %s; margin: 25px 0; color: #334155;">
                                <p style="margin: 5px 0;"><strong>Kiedy:</strong> %s - %s</p>
                                <p style="margin: 5px 0;"><strong>Gdzie:</strong> %s</p>
                            </div>

                            <p style="margin-bottom: 15px; color: #334155;">Dodaj wydarzenie do kalendarza:</p>
                            
                            <div style="margin-top: 10px;">
                                <a href="%s" style="%s">Outlook (Szkolny / Office 365)</a>
                            </div>
                            <div style="margin-top: 10px;">
                                <a href="%s" style="%s">Outlook (Prywatny)</a>
                                <a href="%s" style="%s">Google Calendar</a>
                            </div>
                        </td></tr>
                        <tr><td style="background-color: #f1f5f9; padding: 20px; text-align: center; color: %s; font-size: 12px;">
                             &copy; %s System Samorządu Uczniowskiego.<br>Wiadomość wygenerowana automatycznie.
                        </td></tr>
                    </table>
                </td></tr></table>
                </body></html>
                """.formatted(
                COLOR_BG,
                COLOR_PRIMARY,
                COLOR_PRIMARY, fullName,
                title,
                COLOR_PRIMARY,
                event.getStartDate().toLocalTime(), event.getEndDate().toLocalTime(),
                location,
                office365Link, btnPrimary,
                outlookWebLink, btnSecondary,
                googleLink, btnSecondary,
                COLOR_GRAY,
                java.time.Year.now().getValue()
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