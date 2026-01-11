package start.spring.io.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.service.PenaltyService;

@Controller
@RequestMapping("/penalties")
public class PenaltyController {
    
    private final PenaltyService service;

    public PenaltyController(PenaltyService service) {
        this.service = service;
    }

    /** Lists all penalties. */
    @GetMapping
    public String listPenalties(Model model) {
        model.addAttribute("penalties", service.getAllPenalties());
        model.addAttribute("newPenalty", new Penalty());
        return "penalty-list";
    }

    /** Edit form */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Penalty penalty = service.getPenaltyById(id)
                .orElseThrow(() -> new IllegalArgumentException("Penalty not found: " + id));
        model.addAttribute("penalty", penalty);
        return "penalty-edit";
    }

    /** Saves changes to a penalty */
    @PostMapping(value = {"/edit/{id}"})
    public String editPenalty(@PathVariable Integer id, @ModelAttribute("penalty") Penalty penalty) {
        service.updatePenalty(id, penalty);
        return "redirect:/penalties";
    }

    /** Adds a new penalty */
    @PostMapping("/add")
    public String addPenalty(@ModelAttribute("newPenalty") Penalty penalty) {
        service.createPenalty(penalty);
        return "redirect:/penalties";
    }

    /** Deletes a penalty */
    @GetMapping("/delete/{id}")
    public String deletePenalty(@PathVariable Integer id) {
        service.deletePenalty(id);
        return "redirect:/penalties";
    }
}



