package start.spring.io.backend.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import start.spring.io.backend.dto.FacilityCardView;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.service.FacilityService;
import start.spring.io.backend.service.UserService;

@Controller
@RequestMapping("/facilities")
public class FacilityController {

    private final FacilityService service;
    private final UserService userService;

    public FacilityController(FacilityService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    /** Lista todas las facilities */
    @GetMapping
    public String listFacilities(Model model, Authentication authentication) {
        List<Facility> facilities = service.getAllFacilities();
        List<FacilityCardView> cards = facilities.stream()
                .map(this::toCardView)
                .toList();

        String userName = "";
        if (authentication != null && authentication.isAuthenticated()) {
            userName = userService.getUserByEmail(authentication.getName())
                    .map(start.spring.io.backend.model.User::getName)
                    .orElse("");
        }

        model.addAttribute("facilityCards", cards);
        model.addAttribute("newFacility", new Facility());
        model.addAttribute("userName", userName);
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

    private FacilityCardView toCardView(Facility facility) {
        String type = facility.getType() == null ? "" : facility.getType().toLowerCase();
        String imageUrl = switch (type) {
            case "tennis", "padel" ->
                "https://images.unsplash.com/photo-1508609349937-5ec4ae374ebf?auto=format&fit=crop&w=1000&q=80";
            case "basketball" ->
                "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1000&q=80";
            case "soccer", "football" ->
                "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=1000&q=80";
            case "badminton" ->
                "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1000&q=80";
            default ->
                "https://images.unsplash.com/photo-1471295253337-3ceaaedca402?auto=format&fit=crop&w=1000&q=80";
        };

        String location = switch (type) {
            case "tennis", "padel" -> "Racquet Zone";
            case "basketball" -> "Central Pavilion";
            case "soccer", "football" -> "North Sports Complex";
            case "badminton" -> "Indoor Hall";
            default -> "Main Sports Hub";
        };

        int capacity = switch (type) {
            case "soccer", "football" -> 22;
            case "basketball" -> 10;
            case "tennis", "padel" -> 4;
            case "badminton" -> 6;
            default -> 8;
        };

        boolean available = facility.getStatus() == null
                || facility.getStatus().equalsIgnoreCase("free")
                || facility.getStatus().equalsIgnoreCase("available");
        String statusLabel = available ? "Available" : "Unavailable";
        String statusClass = available ? "status-available" : "status-unavailable";

        return new FacilityCardView(facility, imageUrl, location, capacity, statusLabel, statusClass);
    }
}
