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

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login") 
    public String login() {
        return "login"; // returns the login.html view
    }

    @GetMapping("/signup") 
    public String signup(Model model){
        model.addAttribute("user", new User());
        return "signup"; // returns the signup.html view
    }
    
    @PostMapping("/signup")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes, Model model) {
        try {
            // Check if email already exists
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                model.addAttribute("error", "Email already registered.");
                model.addAttribute("user", user);
                return "signup";
            }

            // Encode the password before saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // Assign default role
            if (user.getRole() == null || user.getRole().isEmpty()) {
                user.setRole("user");
            }

            userRepository.save(user);
            return "redirect:/login?signupSuccess"; // Redirect to login page after successful signup
        } catch (Exception e) {
            model.addAttribute("error", "Error creating account: " + e.getMessage());
            model.addAttribute("user", user);
            return "signup";
        }
    }
}
