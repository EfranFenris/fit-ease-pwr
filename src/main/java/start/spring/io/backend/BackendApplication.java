package start.spring.io.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is the Main Class of the application.
 *
 * <p>It has three important annotations:</p>
 * <ul>
 * <li><b>@SpringBootApplication</b>: Tells Spring to start up, scan for our controllers/services, and configure the database.</li>
 * <li><b>@EnableAsync</b>: Allows tasks to run in the background (like sending emails without freezing the screen).</li>
 * <li><b>@EnableScheduling</b>: Turns on the internal clock so our Penalty Scheduler can run automatically at 3:00 AM.</li>
 * </ul>
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class BackendApplication {

    /**
     * The standard Java main method.
     * When you click "Run" in your IDE, this is the exact line that gets executed first.
     * It launches the Spring Boot server (Tomcat) so your website becomes accessible.
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}