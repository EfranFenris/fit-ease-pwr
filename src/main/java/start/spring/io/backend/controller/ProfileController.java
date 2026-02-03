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

/**
 * This controller allows users to see and edit their own personal profile.
 * It is similar to the Admin's user editor, but restricted so users can only
 * change their own details.
 */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Displays the "My Profile" page.
     * It uses the Authentication object to find out who is currently logged in,
     * fetches their data, and prepares the view.
     */
    @GetMapping
    public String myProfile(Model model, Authentication authentication) {
        // We find the logged-in user by their email
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        model.addAttribute("user", user);

        // We tell the view to submit the form back to "/profile"
        model.addAttribute("postUrl", "/profile");
        // We tell the view we are NOT an admin, so it hides the "Role" dropdown
        model.addAttribute("isAdminMode", false);

        return "user-edit"; // We reuse the existing user-edit.html template
    }

    /**
     * Process the profile updates.
     * We identify the user again to ensure security (so you can't edit
     * someone else's profile by changing the ID in the browser).
     */
    @PostMapping
    public String updateProfile(@ModelAttribute("user") User userDetails, Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userService.getUserByEmail(email).orElseThrow();

        // We update using the ID of the logged-in user to be safe
        userService.updateUser(currentUser.getUserId(), userDetails);

        return "redirect:/facilities"; // Return to the main page or show a success message
    }
}