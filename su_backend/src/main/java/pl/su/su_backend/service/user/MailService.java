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

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final EmailClient emailClient;

    @Value("${ACS_SENDER_ADDRESS}")
    private String senderAddress;

    @Async
    public void sendActivationEmail(String toEmail, String fullName, String activationUrl) {
        log.info("Azure Email: Sending an activation link to {}", toEmail);

        String subject = "Aktywacja konta - System Samorządu";
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>Cześć %s!</h2>
                    <p>Dziękujemy za rejestrację w systemie.</p>
                    <p>Kliknij poniższy przycisk, aby aktywować konto:</p>
                    <a href="%s" style="background-color: #0078D4; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px;">Aktywuj konto</a>
                    <p style="margin-top: 20px; font-size: 12px; color: #666;">Jeśli przycisk nie działa, skopiuj ten link: %s</p>
                </div>
                """.formatted(fullName, activationUrl, activationUrl);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        log.info("Azure Email: Sending a welcome message to {}", toEmail);

        String subject = "Witaj w Systemie Samorządu!";
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>Witaj %s!</h2>
                    <p>Twoje konto zostało pomyślnie utworzone (logowanie przez Microsoft).</p>
                    <p>Możesz już w pełni korzystać z systemu.</p>
                </div>
                """.formatted(fullName);

        sendEmail(toEmail, subject, htmlContent);
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetUrl) {
        log.info("Azure Email: Sending a password reset to {}", toEmail);

        String subject = "Reset hasła - System Samorządu";
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>Cześć %s!</h2>
                    <p>Otrzymaliśmy prośbę o zresetowanie hasła do Twojego konta.</p>
                    <p>Kliknij poniższy przycisk, aby ustawić nowe hasło:</p>
                    <a href="%s" style="background-color: #D83B01; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px;">Zresetuj hasło</a>
                    <p style="margin-top: 20px; font-size: 12px; color: #666;">Link jest ważny przez 24 godziny. Jeśli to nie Ty wysłałeś prośbę, zignoruj tę wiadomość.</p>
                    <p style="font-size: 12px; color: #666;">Link: %s</p>
                </div>
                """.formatted(fullName, resetUrl, resetUrl);

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
            PollResponse<EmailSendResult> result = poller.waitForCompletion();

            log.info("Azure Email Status: {} | MessageId: {}", result.getStatus(), result.getValue().getId());

        } catch (Exception e) {
            log.error("Error sending email via Azure to {}: {}", toEmail, e.getMessage());
        }
    }
}