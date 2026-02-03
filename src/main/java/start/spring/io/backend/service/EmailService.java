package start.spring.io.backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * This service handles sending emails.
 * It is used for booking confirmations, maintenance alerts, and penalty notifications.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an email asynchronously.
     * @Async is very important. It sends it in the background".
     * Without it, the user would have to wait 5 seconds for the email to send
     * before the webpage would reload.
     */
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom("fiteasepwr@gmail.com"); // The sender address
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + to);

        } catch (Exception e) {
            // If the internet is down or the email is wrong, we just print the error
            // instead of crashing the whole application.
            System.err.println("❌ Error sending email to " + to + ": " + e.getMessage());
        }
    }
}