package start.spring.io.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.model.User;
import start.spring.io.backend.service.PenaltyService;
import start.spring.io.backend.service.UserService;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/penalties")
public class PenaltyController {

    private final PenaltyService service;
    private final UserService userService; // <--- NECESARIO para buscar al usuario

    public PenaltyController(PenaltyService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    // --- LISTADO (Opcional, el admin usa el Dashboard) ---
    @GetMapping
    public String listPenalties(Model model) {
        model.addAttribute("penalties", service.getAllPenalties());
        model.addAttribute("newPenalty", new Penalty());
        return "penalty-list";
    }

    // --- AÑADIR PENALIZACIÓN MANUAL (CORREGIDO) ---
    @PostMapping("/add")
    public String addPenalty(@ModelAttribute("newPenalty") Penalty penalty,
                             @RequestParam("userId") Integer userId) { // <--- Recibimos el ID del formulario

        // 1. Buscamos el usuario por ID
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid User ID: " + userId));

        // 2. Asignamos el usuario a la penalización
        penalty.setUser(user);

        // 3. Si no se puso fecha, ponemos AHORA
        if (penalty.getDatehour() == null) {
            penalty.setDatehour(LocalDateTime.now());
        }

        // 4. Guardamos
        service.createPenalty(penalty);

        // 5. Redirigimos al Dashboard del Admin (no a /penalties)
        return "redirect:/reservations/manager";
    }

    @GetMapping("/delete/{id}")
    public String deletePenalty(@PathVariable Integer id) {
        service.deletePenalty(id);
        return "redirect:/reservations/manager"; // Redirigimos al dashboard
    }

    // Edición (Opcional)
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Penalty penalty = service.getPenaltyById(id)
                .orElseThrow(() -> new IllegalArgumentException("Penalty not found: " + id));
        model.addAttribute("penalty", penalty);
        return "penalty-edit";
    }

    @PostMapping("/edit/{id}")
    public String editPenalty(@PathVariable Integer id, @ModelAttribute("penalty") Penalty penalty) {
        service.updatePenalty(id, penalty);
        return "redirect:/reservations/manager";
    }
}