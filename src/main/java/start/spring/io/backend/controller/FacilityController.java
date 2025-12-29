package start.spring.io.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.service.FacilityService;

@Controller
@RequestMapping("/facilities")
public class FacilityController {

    private final FacilityService service;

    public FacilityController(FacilityService service) {
        this.service = service;
    }

    /** Lista todas las facilities */
    @GetMapping
    public String listFacilities(Model model) {
        model.addAttribute("facilities", service.getAllFacilities());
        model.addAttribute("newFacility", new Facility());
        return "facility-list";
    }

    /** Formulario de edición */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Facility facility = service.getFacilityById(id)
                .orElseThrow(() -> new IllegalArgumentException("Facility no encontrada: " + id));
        model.addAttribute("facility", facility);
        return "facility-edit";
    }

    /** Guardar cambios de edición */
    @PostMapping("/edit/{id}")
    public String editFacility(@PathVariable Integer id, @ModelAttribute("facility") Facility facility) {
        service.updateFacility(id, facility);
        return "redirect:/facilities";
    }

    /** Añadir nueva facility */
    @PostMapping("/add")
    public String addFacility(@ModelAttribute("newFacility") Facility facility) {
        service.createFacility(facility);
        return "redirect:/facilities";
    }

    /** Borrar facility */
    @GetMapping("/delete/{id}")
    public String deleteFacility(@PathVariable Integer id) {
        service.deleteFacility(id);
        return "redirect:/facilities";
    }
}