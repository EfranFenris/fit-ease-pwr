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

@Configuration
public class AuthenticationHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    // Inyectamos el servicio para poder buscar el nombre
    public AuthenticationHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

        // --- NUEVO: Guardamos el nombre real en la sesión ---
        String email = authentication.getName();
        userService.getUserByEmail(email).ifPresent(user -> {
            HttpSession session = request.getSession();
            session.setAttribute("fullName", user.getName()); // Guardamos "Juan" bajo la clave "fullName"
        });
        // ----------------------------------------------------

        if (roles.contains("ROLE_maintenance")) {
            response.sendRedirect("/maintenance-requests");
        } else if(roles.contains("ROLE_admin")){
            response.sendRedirect("/reservations/manager");
        } else {
            response.sendRedirect("/facilities");
        }
    }
}