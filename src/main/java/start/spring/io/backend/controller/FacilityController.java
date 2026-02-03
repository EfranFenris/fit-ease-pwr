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

/**
 * This controller manages the Facilities page.
 * It handles listing all the sports facilities (courts, fields) and allows
 * the manager to change their status (Open/Closed).
 */
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

    /**
     * Displays the list of all facilities.
     * It fetches data from the database, converts it into a "card" and sends it to the "facility-list.html" page.
     */
    @GetMapping
    public String listFacilities(Model model, Authentication authentication) {
        // We get all facilities and transform them into 'FacilityCardView' objects
        // which are easier to display on the webpage.
        List<FacilityCardView> facilities = service.getAllFacilities().stream()
                .map(this::toCardView)
                .toList();

        model.addAttribute("facilityCards", facilities);
        model.addAttribute("currentPage", "facilities");

        return "facility-list";
    }

    /**
     * Changes the status of a facility between "Available" and "Unavailable".
     * If a facility is being closed, we also cancel any upcoming reservations
     * to avoid conflicts.
     */
    @PostMapping("/status/{id}/toggle")
    public String toggleStatus(@PathVariable Integer id) {
        service.getFacilityById(id).ifPresent(facility -> {
            boolean isAvailable = "Available".equalsIgnoreCase(facility.getStatus())
                    || "Free".equalsIgnoreCase(facility.getStatus());

            if (isAvailable) {
                // If it was available, we close it now.
                facility.setStatus("Unavailable");
                service.updateFacility(id, facility);

                // Important Business Logic: Cancel future reservations and notify users.
                reservationService.cancelReservationsForFacility(id, "Facility closed by Reservation Manager.");

            } else {
                // If it was closed, we open it up again.
                facility.setStatus("Available");
                service.updateFacility(id, facility);
            }
        });
        return "redirect:/facilities";
    }

    /**
     * Helper method to convert a database 'Facility' object into a 'FacilityCardView'.
     * This handles the visual logic, like choosing which image to show
     * and deciding if the status label should be Green (Available), Red (Unavailable),
     * or Orange (Maintenance).
     */
    private FacilityCardView toCardView(Facility facility) {
        String type = facility.getType() != null ? facility.getType().toLowerCase() : "";

        // Choose a background image based on the sport type
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

        // Check if there is an active maintenance request for this facility
        boolean hasActiveMaintenance = maintenanceService.hasActiveRequests(facility.getFacilityId());

        // Check what the database says about the status
        boolean isStatusAvailable = "Available".equalsIgnoreCase(facility.getStatus())
                || "Free".equalsIgnoreCase(facility.getStatus());

        // Real availability: It is only truly available if the DB says so AND there is no maintenance
        boolean actuallyAvailable = isStatusAvailable && !hasActiveMaintenance;

        String statusLabel;
        String statusClass;

        if (hasActiveMaintenance) {
            // If maintenance is active, show that regardless of anything else
            statusLabel = "Under Maintenance";
            statusClass = "status-maintenance"; // CSS class for orange color
        } else if (actuallyAvailable) {
            // If it is truly free
            statusLabel = "Available";
            statusClass = "status-available";   // CSS class for green color
        } else {
            // Closed by manager
            statusLabel = "Unavailable";
            statusClass = "status-unavailable"; // CSS class for red/gray color
        }

        // Return the DTO
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