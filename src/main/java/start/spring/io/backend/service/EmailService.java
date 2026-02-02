package start.spring.io.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía un correo electrónico de forma asíncrona (no bloquea la web).
     */
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom("miryammerchanleon@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + to);

        } catch (Exception e) {
            System.err.println("❌ Error sending email to " + to + ": " + e.getMessage());
        }
    }
}