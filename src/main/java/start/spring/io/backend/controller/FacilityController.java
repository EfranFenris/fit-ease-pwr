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
import start.spring.io.backend.service.MaintenanceRequestService;
import start.spring.io.backend.service.UserService;

@Controller
@RequestMapping("/facilities")
public class FacilityController {

    private final FacilityService service;
    private final UserService userService;
    private final MaintenanceRequestService maintenanceService; // 1. Inyectar nuevo servicio

    // 2. Actualizar constructor
    public FacilityController(FacilityService service, UserService userService, MaintenanceRequestService maintenanceService) {
        this.service = service;
        this.userService = userService;
        this.maintenanceService = maintenanceService;
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
        model.addAttribute("currentPage", "facilities");
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
        // Usamos el tipo, pero protegemos contra nulos y pasamos a minúsculas
        String type = facility.getType() == null ? "" : facility.getType().toLowerCase();

        // --- 1. Determinar Imagen (Lógica "Contains") ---
        String imageUrl;
        if (type.contains("tennis") || type.contains("padel")) {
            imageUrl = "https://images.unsplash.com/photo-1508609349937-5ec4ae374ebf?auto=format&fit=crop&w=1000&q=80";
        } else if (type.contains("basketball")) {
            imageUrl = "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1000&q=80";
        } else if (type.contains("soccer") || type.contains("football")) {
            imageUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=1000&q=80";
        } else if (type.contains("badminton")) {
            imageUrl = "https://images.unsplash.com/photo-1626248316686-e74659b8eb2c?auto=format&fit=crop&w=1000&q=80"; // He cambiado la foto para que no sea igual a basket
        } else {
            // Default image
            imageUrl = "https://images.unsplash.com/photo-1471295253337-3ceaaedca402?auto=format&fit=crop&w=1000&q=80";
        }

        // --- 2. Determinar Ubicación (Lógica "Contains") ---
        String location;
        if (type.contains("tennis") || type.contains("padel")) {
            location = "Racquet Zone";
        } else if (type.contains("basketball")) {
            location = "Central Pavilion";
        } else if (type.contains("soccer") || type.contains("football")) {
            location = "North Sports Complex";
        } else if (type.contains("badminton") || type.contains("ping")) {
            location = "Indoor Hall";
        } else {
            location = "Main Sports Hub";
        }

        // --- 3. Determinar Capacidad (Usando el Servicio centralizado) ---
        // Esto asegura que el "Max Participants" del formulario coincida con lo que muestras en la tarjeta
        int capacity = service.getCapacityForType(type);

        // --- 4. Estado ---
        boolean available = facility.getStatus() == null
                || facility.getStatus().equalsIgnoreCase("free")
                || facility.getStatus().equalsIgnoreCase("available");
        String statusLabel = available ? "Available" : "Unavailable";
        String statusClass = available ? "status-available" : "status-unavailable";

        // 3. Consultar si hay mantenimiento activo
        boolean hasActiveMaintenance = maintenanceService.hasActiveRequests(facility.getFacilityId());

        // 4. Pasar el nuevo booleano al constructor del DTO
        return new FacilityCardView(facility, imageUrl, location, capacity, statusLabel, statusClass, hasActiveMaintenance);
    }

    /** Toggle status between Available and Unavailable (Solo Admin) */
    @PostMapping("/status/{id}/toggle")
    public String toggleStatus(@PathVariable Integer id) {
        service.getFacilityById(id).ifPresent(facility -> {
            // Si es 'Available' o 'Free' pasa a 'Unavailable', si no, vuelve a 'Available'
            boolean isAvailable = "Available".equalsIgnoreCase(facility.getStatus())
                    || "Free".equalsIgnoreCase(facility.getStatus());

            facility.setStatus(isAvailable ? "Unavailable" : "Available");
            service.updateFacility(id, facility);
        });
        return "redirect:/facilities";
    }
}
