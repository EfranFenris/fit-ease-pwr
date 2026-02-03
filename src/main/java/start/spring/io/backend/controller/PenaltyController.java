package start.spring.io.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.model.User;
import start.spring.io.backend.service.PenaltyService;
import start.spring.io.backend.service.UserService;

import java.time.LocalDateTime;

/**
 * This controller manages Penalties (punishments).
 * Admins use this to record when a user breaks the rules, like missing a reservation.
 */
@Controller
@RequestMapping("/penalties")
public class PenaltyController {

    private final PenaltyService service;
    private final UserService userService;

    public PenaltyController(PenaltyService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    /**
     * Shows a list of all penalties.
     */
    @GetMapping
    public String listPenalties(Model model) {
        model.addAttribute("penalties", service.getAllPenalties());
        model.addAttribute("newPenalty", new Penalty());
        return "penalty-list";
    }

    /**
     * Adds a new penalty manually.
     * This method receives the form data, finds the user by their ID,
     * assigns the penalty to them, and saves it.
     */
    @PostMapping("/add")
    public String addPenalty(@ModelAttribute("newPenalty") Penalty penalty,
                             @RequestParam("userId") Integer userId) { // <--- Recibimos el ID del formulario

        // Find the user in the database
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid User ID: " + userId));

        // Link the penalty to this specific user
        penalty.setUser(user);

        // If the admin didn't pick a date, use now
        if (penalty.getDatehour() == null) {
            penalty.setDatehour(LocalDateTime.now());
        }

        // Save to database
        service.createPenalty(penalty);

        // Redirect back to the Dashboard (not the penalty list)
        return "redirect:/reservations/manager";
    }

    /**
     * Deletes a penalty by its ID.
     * Useful if the admin made a mistake.
     */
    @GetMapping("/delete/{id}")
    public String deletePenalty(@PathVariable Integer id) {
        service.deletePenalty(id);
        return "redirect:/reservations/manager"; // Redirect back to dashboard
    }

    /**
     * Shows the form to edit an existing penalty.
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Penalty penalty = service.getPenaltyById(id)
                .orElseThrow(() -> new IllegalArgumentException("Penalty not found: " + id));
        model.addAttribute("penalty", penalty);
        return "penalty-edit";
    }

    /**
     * Saves the changes made to a penalty.
     */
    @PostMapping("/edit/{id}")
    public String editPenalty(@PathVariable Integer id, @ModelAttribute("penalty") Penalty penalty) {
        service.updatePenalty(id, penalty);
        return "redirect:/reservations/manager";
    }
}