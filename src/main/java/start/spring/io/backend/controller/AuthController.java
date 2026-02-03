package start.spring.io.backend.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.UserRepository;

/**
 * This controller handles User Authentication (Login and Signup).
 * It manages the pages where users enter their credentials and processes
 * new account registrations.
 */
@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Displays the Login page.
     * Returns the "login.html" view when the user goes to "/login".
     */
    @GetMapping("/login") 
    public String login() {
        return "login"; // returns the login.html view
    }

    /**
     * Displays the Signup (Registration) page.
     * We send an empty 'User' object to the form so the inputs have somewhere
     * to store the data the user types in.
     */
    @GetMapping("/signup") 
    public String signup(Model model){
        model.addAttribute("user", new User());
        return "signup"; // returns the signup.html view
    }

    /**
     * Processes the registration form submission.
     * This receives the data filled by the user, checks if the email is taken,
     * encrypts the password for security, and saves the new user to the database.
     */
    @PostMapping("/signup")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes, Model model) {
        try {
            // Check if the email is already in the database to avoid duplicates
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                // If it exists, show an error message on the signup page
                model.addAttribute("error", "Email already registered.");
                model.addAttribute("user", user);
                return "signup";
            }

            // We use the encoder to convert the password from plaintext into a secure hash.
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // If no role is selected, we assign the default "user" role
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("user");
            }

            // Save the complete user object to the database
            userRepository.save(user);
            return "redirect:/login?signupSuccess"; // Redirect the user to the login page with a success signal
        } catch (Exception e) {
            // If something unexpected goes wrong, show the error message
            model.addAttribute("error", "Error creating account: " + e.getMessage());
            model.addAttribute("user", user);
            return "signup";
        }
    }
}
