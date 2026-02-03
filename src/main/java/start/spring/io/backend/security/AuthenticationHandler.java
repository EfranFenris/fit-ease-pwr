package start.spring.io.backend.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession; // Importante
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import start.spring.io.backend.service.UserService; // Importante

import java.io.IOException;
import java.util.Set;

/**
 * This class is the traffic controller for after a successful login.
 * Once the user enters the correct password, this class runs to decide
 * where they should be redirected based on their Role.
 */
@Configuration
public class AuthenticationHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    // We inject the UserService so we can look up the user's real name
    public AuthenticationHandler(UserService userService) {
        this.userService = userService;
    }

    /**
     * This method runs automatically the moment the user logs in successfully.
     * It does two things:
     * 1. Saves the user's real name to the "Session" (so we can display their name in the HTML).
     * 2. Redirects them to their specific dashboard (Admin vs User vs Maintenance).
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // Save the name in the session
        String email = authentication.getName();
        userService.getUserByEmail(email).ifPresent(user -> {
            HttpSession session = request.getSession();
            // We store the name so the HTML pages can read it later
            session.setAttribute("fullName", user.getName());
        });

        // Redirect logic: "If you are X role, go to page Y"
        if (roles.contains("ROLE_maintenance")) {
            response.sendRedirect("/maintenance-requests");
        } else if(roles.contains("ROLE_admin")){
            response.sendRedirect("/reservations/manager");
        } else {
            // Regular users go to the facility list
            response.sendRedirect("/facilities");
        }
    }
}