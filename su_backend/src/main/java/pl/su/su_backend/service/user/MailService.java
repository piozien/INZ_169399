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

    private void sendEmail(String to, String subject, String body) {
        try {
            EmailMessage message = new EmailMessage()
                    .setSenderAddress(fromEmail)
                    .setToRecipients(to)
                    .setSubject(subject)
                    .setBodyPlainText(body);

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


    public void sendWelcomeEmail(String to, String fullName) {
        sendEmail(
                to,
                "Witamy w systemie Samorządu Uczniowskiego",
                "Cześć " + (fullName != null ? fullName : to) + ",\n\n" +
                        "Twoje konto zostało utworzone.\n\n" +
                        "Pozdrawiamy,\nZespół SU"
        );
    }

    public void sendActivationEmail(String to, String fullName, String activationUrl) {
        sendEmail(
                to,
                "Aktywacja konta - Samorząd Uczniowski",
                "Cześć " + (fullName != null ? fullName : to) + ",\n\n" +
                        "Aby aktywować konto kliknij w link:\n" + activationUrl + "\n\n" +
                        "Pozdrawiamy,\nZespół SU"
        );
    }

    public void sendPasswordResetEmail(String to, String fullName, String resetUrl) {
        sendEmail(
                to,
                "Resetowanie hasła - Samorząd Uczniowski",
                "Cześć " + (fullName != null ? fullName : to) + ",\n\n" +
                        "Aby zresetować hasło kliknij w link:\n" + resetUrl + "\n\n" +
                        "Pozdrawiamy,\nZespół SU"
        );
    }
}
