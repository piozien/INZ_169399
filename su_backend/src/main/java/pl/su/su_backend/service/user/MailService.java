package pl.su.su_backend.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@su-app.local}")
    private String fromEmail;

    public void sendWelcomeEmail(String to, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Witamy w systemie Samorządu Uczniowskiego");
            message.setText("Cześć " + (fullName != null ? fullName : to) + ",\n\n" +
                    "Twoje konto zostało utworzone. Jeśli to nie Ty, skontaktuj się z administratorem.\n\n" +
                    "Pozdrawiamy,\nZespół SU");
            mailSender.send(message);
            log.info("Welcome email sent to {}", to);
        } catch (Exception e) {
            log.warn("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }

    public void sendActivationEmail(String to, String fullName, String activationUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Aktywacja konta - Samorząd Uczniowski");
            message.setText("Cześć " + (fullName != null ? fullName : to) + ",\n\n" +
                    "Aby aktywować konto kliknij w link:\n" + activationUrl + "\n\n" +
                    "Link ważny 24h. Jeśli to nie Ty, zignoruj tę wiadomość.\n\n" +
                    "Pozdrawiamy,\nZespół SU");
            mailSender.send(message);
            log.info("Activation email sent to {}", to);
        } catch (Exception e) {
            log.warn("Failed to send activation email to {}: {}", to, e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String fullName, String resetUrl) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Resetowanie hasła - Samorząd Uczniowski");
            message.setText("Cześć " + (fullName != null ? fullName : to) + ",\n\n" +
                    "Aby zresetować hasło kliknij w link:\n" + resetUrl + "\n\n" +
                    "Link ważny 24h. Jeśli to nie Ty, zignoruj tę wiadomość.\n\n" +
                    "Pozdrawiamy,\nZespół SU");
            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (Exception e) {
            log.warn("Failed to send password reset email to {}: {}", to, e.getMessage());
        }
    }
}


