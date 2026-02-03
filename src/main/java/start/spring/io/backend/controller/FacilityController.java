package start.spring.io.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import start.spring.io.backend.dto.FacilityCardView;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.service.FacilityService;
import start.spring.io.backend.service.MaintenanceRequestService;
import start.spring.io.backend.service.ReservationService;
import start.spring.io.backend.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/facilities")
public class FacilityController {

    private final FacilityService service;
    private final UserService userService;
    private final MaintenanceRequestService maintenanceService;
    private final ReservationService reservationService;

    public FacilityController(FacilityService service,
                              UserService userService,
                              MaintenanceRequestService maintenanceService,
                              ReservationService reservationService) {
        this.service = service;
        this.userService = userService;
        this.maintenanceService = maintenanceService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public String listFacilities(Model model, Authentication authentication) {
        // Obtenemos todas las facilities y las transformamos al DTO para la vista
        List<FacilityCardView> facilities = service.getAllFacilities().stream()
                .map(this::toCardView)
                .toList();

        // Depuración rápida: Si esto imprime 0 en la consola, Hibernate no está encontrando datos
        System.out.println("Facilities encontradas: " + facilities.size());

        model.addAttribute("facilityCards", facilities);
        model.addAttribute("currentPage", "facilities");

        return "facility-list";
    }

    @PostMapping("/status/{id}/toggle")
    public String toggleStatus(@PathVariable Integer id) {
        service.getFacilityById(id).ifPresent(facility -> {
            boolean isAvailable = "Available".equalsIgnoreCase(facility.getStatus())
                    || "Free".equalsIgnoreCase(facility.getStatus());

            if (isAvailable) {
                // Si estaba disponible, la deshabilitamos
                facility.setStatus("Unavailable");
                service.updateFacility(id, facility);

                // Cancelar reservas futuras y notificar (Lógica de negocio importante)
                reservationService.cancelReservationsForFacility(id, "Facility closed by Reservation Manager.");

            } else {
                // Si estaba no disponible, la habilitamos
                facility.setStatus("Available");
                service.updateFacility(id, facility);
            }
        });
        return "redirect:/facilities";
    }

    /**
     * Convierte la entidad Facility en un objeto de vista (DTO) controlando la lógica visual.
     */
    private FacilityCardView toCardView(Facility facility) {
        String type = facility.getType() != null ? facility.getType().toLowerCase() : "";

        String imageUrl = "https://images.unsplash.com/photo-1471295253337-3ceaaedca402?auto=format&fit=crop&w=1000&q=80"; // Default
        if (type.contains("tennis")) {
            imageUrl = "https://images.unsplash.com/flagged/photo-1576972405668-2d020a01cbfa?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8OHx8dGVubmlzfGVufDB8fDB8fHww";
        } else if(type.contains("padel")){
            imageUrl = "https://images.unsplash.com/photo-1612534847738-b3af9bc31f0c?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8M3x8cGFkZWx8ZW58MHx8MHx8fDA%3D";
        }else if (type.contains("basketball")) {
            imageUrl = "https://images.unsplash.com/photo-1546519638-68e109498ffc?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8YmFza2V0YmFsbHxlbnwwfHwwfHx8MA%3D%3D";
        } else if (type.contains("soccer") || type.contains("football")) {
            imageUrl = "https://images.unsplash.com/photo-1553778263-73a83bab9b0c?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D";
        } else if (type.contains("badminton")) {
            imageUrl = "https://media.istockphoto.com/id/1837099474/photo/badminton-serve.jpg?s=612x612&w=0&k=20&c=xtFHN5R7iMVhHtSIkhA3W5zh3kS1u2Pn4eN7BnHafs0=";
        }else if(type.contains("ping pong")) {
            imageUrl = "https://images.unsplash.com/photo-1609710228159-0fa9bd7c0827?fm=jpg&q=60&w=3000&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8Mnx8cGluZyUyMHBvbmd8ZW58MHx8MHx8fDA%3D";
        }

        String location = "Sports Hub";

        // --- 2. Lógica de Estado (Corrección Principal) ---

        // Verificamos si hay mantenimiento activo
        boolean hasActiveMaintenance = maintenanceService.hasActiveRequests(facility.getFacilityId());

        // Verificamos qué dice la base de datos sobre el estado
        boolean isStatusAvailable = "Available".equalsIgnoreCase(facility.getStatus())
                || "Free".equalsIgnoreCase(facility.getStatus());

        // El estado real: Disponible solo si la BD dice OK y NO hay mantenimiento
        boolean actuallyAvailable = isStatusAvailable && !hasActiveMaintenance;

        String statusLabel;
        String statusClass;

        if (hasActiveMaintenance) {
            // Prioridad 1: Si hay mantenimiento, se muestra mantenimiento
            statusLabel = "Under Maintenance";
            statusClass = "status-maintenance"; // Asegúrate de tener estilo CSS para esto (ej. color naranja)
        } else if (actuallyAvailable) {
            // Prioridad 2: Si está realmente disponible
            statusLabel = "Available";
            statusClass = "status-available";   // Color verde
        } else {
            // Prioridad 3: No disponible por otra razón (ej. cerrado por manager)
            statusLabel = "Unavailable";
            statusClass = "status-unavailable"; // Color rojo/gris
        }

        // Devolvemos el DTO
        return new FacilityCardView(
                facility,
                imageUrl,
                location,
                service.getCapacityForType(facility.getType()),
                statusLabel,
                statusClass,
                hasActiveMaintenance
        );
    }
}