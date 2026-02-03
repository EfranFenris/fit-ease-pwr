package start.spring.io.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * This controller handles the "Home" or "Root" URL (the main website address).
 * It decides where users should go when they first arrive.
 */
@Controller
public class HomeController {

    /**
     * Handles requests to the root path ("/").
     * If the user is already logged in, they are sent directly to the app (Facilities).
     * If they are a guest, they are shown the Landing Page (Welcome screen).
     */
    @GetMapping("/")
    public String home(Authentication authentication) {
        // Check if the user is currently logged in
        if (authentication != null && authentication.isAuthenticated()) {
            // Redirect them to the main dashboard
            return "redirect:/facilities";
        }
        // Otherwise, show the public landing page
        return "landing";
    }
}   