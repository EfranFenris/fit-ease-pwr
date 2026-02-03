package start.spring.io.backend.config; // O tu paquete correspondiente

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * This class handles the general configuration settings for the application.
 * The @Configuration annotation tells Spring that this class creates "Beans" (tools)
 * that will be used in other parts of the code.
 */
@Configuration
public class ApplicationConfig {

    /**
     * This method provides a tool to encrypt passwords.
     * We use BCrypt so that passwords are stored securely in the database,
     * rather than as plain text (like "12345"), which would be dangerous.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}