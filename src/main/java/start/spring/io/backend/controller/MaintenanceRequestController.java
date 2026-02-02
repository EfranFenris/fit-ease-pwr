package start.spring.io.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.model.User;
import start.spring.io.backend.service.EmailService; // <--- Importar
import start.spring.io.backend.service.FacilityService;
import start.spring.io.backend.service.MaintenanceRequestService;
import start.spring.io.backend.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/maintenance-requests")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceService;
    private final FacilityService facilityService;
    private final UserService userService;
    private final EmailService emailService; // <--- Inyectar servicio

    public MaintenanceRequestController(MaintenanceRequestService maintenanceService,
                                        FacilityService facilityService,
                                        UserService userService,
                                        EmailService emailService) {
        this.maintenanceService = maintenanceService;
        this.facilityService = facilityService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping
    public String listRequests(Model model, @RequestParam(value = "filter", required = false) String filter) {
        List<MaintenanceRequest> requests;
        if (filter != null && !filter.isEmpty()) {
            requests = maintenanceService.getFilteredRequests(filter);
        } else {
            requests = maintenanceService.getAllRequests();
        }
        model.addAttribute("requests", requests);
        model.addAttribute("filter", filter);
        model.addAttribute("currentPage", "maintenance");
        return "maintenance-list";
    }

    @GetMapping("/maintenance-request-form/{facilityId}")
    public String showRequestForm(@PathVariable Integer facilityId, Model model) {
        Optional<Facility> facility = facilityService.getFacilityById(facilityId);
        if (facility.isPresent()) {
            MaintenanceRequest maintenanceRequest = new MaintenanceRequest();
            maintenanceRequest.setFacility(facility.get());
            model.addAttribute("maintenanceRequest", maintenanceRequest);
            model.addAttribute("facilityName", facility.get().getName());
            return "maintenance-request-form";
        } else {
            return "redirect:/facilities";
        }
    }

    @PostMapping("/add")
    public String addRequest(@ModelAttribute MaintenanceRequest maintenanceRequest,
                             @RequestParam("facilityId") Integer facilityId,
                             Authentication authentication) {

        // 1. Vincular Facility
        Facility facility = facilityService.getFacilityById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Facility ID"));
        maintenanceRequest.setFacility(facility);

        // 2. Vincular Usuario (el que está logueado)
        String userEmail = "";
        String userName = "User";

        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.getUserByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                maintenanceRequest.setUser(user);
                userEmail = user.getEmail();
                userName = user.getName();
            }
        }

        // 3. Datos automáticos
        maintenanceRequest.setReportDate(LocalDateTime.now());
        maintenanceRequest.setStatus("PENDING");

        // 4. Guardar
        maintenanceService.createRequest(maintenanceRequest);

        // 5. ENVIAR CORREO DE CONFIRMACIÓN
        if (!userEmail.isEmpty()) {
            String subject = "Maintenance Request Received: " + facility.getName();
            String body = "Hello " + userName + ",\n\n" +
                    "We have received your maintenance report for " + facility.getName() + ".\n" +
                    "Issue: " + maintenanceRequest.getIssueType() + "\n\n" +
                    "Our maintenance team will review it shortly.\n" +
                    "Thank you for helping us keep FitEasePWR in top shape!\n\n" +
                    "Best regards,\nFitEasePWR Team";

            emailService.sendEmail(userEmail, subject, body);
        }

        return "redirect:/facilities";
    }

    // Métodos para actualizar estado (solo admin/maintenance)
    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Integer id, @RequestParam("status") String status) {
        maintenanceService.updateRequestStatus(id, status);
        return "redirect:/maintenance-requests";
    }
}