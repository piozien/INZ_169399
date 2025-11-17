package pl.su.su_backend.service.user;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.util.polling.SyncPoller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailService {

    private final EmailClient emailClient;

    @Value("${ACS_SENDER_ADDRESS}")
    private String fromEmail;

    public MailService(@Value("${ACS_CONNECTION_STRING}") String connectionString) {
        this.emailClient = new EmailClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            EmailMessage message = new EmailMessage()
                    .setSenderAddress(fromEmail)
                    .setToRecipients(to)
                    .setSubject(subject)
                    .setBodyHtml(htmlBody);

            SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
            poller.waitForCompletion();

            EmailSendResult result = poller.getFinalResult();
            if (result != null && result.getStatus() == EmailSendStatus.SUCCEEDED) {
                log.info("Email sent successfully to {}", to);
            } else {
                log.warn("Email to {} did not complete successfully: {}", to, result != null ? result.getStatus() : "UNKNOWN");
            }
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
    
    private String createHtmlTemplate(String title, String content, String buttonUrl, String buttonText) {
        String buttonHtml = "";
        if (buttonUrl != null && buttonText != null) {
            buttonHtml = "<a href=\"" + buttonUrl + "\" style=\"background-color: #007bff; color: #ffffff; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 20px;\">" + buttonText + "</a>";
        }

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; color: #333; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }" +
                ".header { background-color: #004aad; color: #ffffff; padding: 20px; text-align: center; }" +
                ".content { padding: 30px; line-height: 1.6; }" +
                ".footer { background-color: #f4f4f4; color: #777; padding: 20px; text-align: center; font-size: 12px; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\"><h1>" + title + "</h1></div>" +
                "<div class=\"content\">" + content + (buttonHtml.isEmpty() ? "" : "<br><br><div style=\"text-align: center;\">" + buttonHtml + "</div>") + "</div>" +
                "<div class=\"footer\"><p>Wiadomość wygenerowana automatycznie przez system Samorządu Uczniowskiego.</p></div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }


    public void sendWelcomeEmail(String to, String fullName) {
        String title = "Witamy w systemie SU!";
        String content = "Cześć " + (fullName != null ? fullName : to) + ",<br><br>" +
                "Twoje konto w systemie Samorządu Uczniowskiego zostało pomyślnie utworzone. Możesz teraz zalogować się i korzystać z dostępnych funkcji.<br><br>" +
                "Pozdrawiamy,<br>Zespół SU";
        String htmlBody = createHtmlTemplate(title, content, null, null);
        sendEmail(to, "Witamy w systemie Samorządu Uczniowskiego", htmlBody);
    }

    public void sendActivationEmail(String to, String fullName, String activationUrl) {
        String title = "Aktywacja Konta";
        String content = "Cześć " + (fullName != null ? fullName : to) + ",<br><br>" +
                "Dziękujemy za rejestrację. Aby aktywować swoje konto, kliknij poniższy przycisk:";
        String htmlBody = createHtmlTemplate(title, content, activationUrl, "Aktywuj Konto");
        sendEmail(to, "Aktywacja konta - Samorząd Uczniowski", htmlBody);
    }

    public void sendPasswordResetEmail(String to, String fullName, String resetUrl) {
        String title = "Resetowanie Hasła";
        String content = "Cześć " + (fullName != null ? fullName : to) + ",<br><br>" +
                "Otrzymaliśmy prośbę o zresetowanie hasła do Twojego konta. Aby kontynuować, kliknij poniższy przycisk:";
        String htmlBody = createHtmlTemplate(title, content, resetUrl, "Zresetuj Hasło");
        sendEmail(to, "Resetowanie hasła - Samorząd Uczniowski", htmlBody);
    }
}
