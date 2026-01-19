package start.spring.io.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        // Si el usuario está autenticado, redirige a facilities
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/facilities";
        }
        // Si no está autenticado, muestra la landing page
        return "landing";
    }
}   