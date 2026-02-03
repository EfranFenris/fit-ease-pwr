package start.spring.io.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import start.spring.io.backend.model.User;
import start.spring.io.backend.service.UserService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String myProfile(Model model, Authentication authentication) {
        // Buscamos al usuario logueado por su email
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        model.addAttribute("user", user);

        // TRUCO: Le decimos a la vista que envíe el formulario a /profile
        model.addAttribute("postUrl", "/profile");
        // Le indicamos que NO estamos en modo admin para ocultar el selector de rol
        model.addAttribute("isAdminMode", false);

        return "user-edit"; // Reutilizamos la misma vista
    }

    @PostMapping
    public String updateProfile(@ModelAttribute("user") User userDetails, Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email).orElseThrow();

        // Actualizamos usando el ID del usuario logueado (seguridad)
        userService.updateUser(currentUser.getUserId(), userDetails);

        return "redirect:/facilities"; // O volver a /profile con mensaje de éxito
    }
}