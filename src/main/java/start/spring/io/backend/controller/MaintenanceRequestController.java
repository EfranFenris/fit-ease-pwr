package start.spring.io.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.model.User;
import start.spring.io.backend.service.EmailService;
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
    private final EmailService emailService;

    public MaintenanceRequestController(MaintenanceRequestService maintenanceService,
                                        FacilityService facilityService,
                                        UserService userService,
                                        EmailService emailService) {
        this.maintenanceService = maintenanceService;
        this.facilityService = facilityService;
        this.userService = userService;
        this.emailService = emailService;
    }

    // LISTADO (Sin cambios)
    @GetMapping
    public String listRequests(Model model, @RequestParam(value = "status", required = false) String filter) {
        List<MaintenanceRequest> allRequests = maintenanceService.getAllRequests();

        long pendingCount = allRequests.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();
        long inprogressCount = allRequests.stream().filter(r -> "IN_PROGRESS".equalsIgnoreCase(r.getStatus())).count();
        long resolvedCount = allRequests.stream().filter(r -> "RESOLVED".equalsIgnoreCase(r.getStatus())).count();

        List<MaintenanceRequest> displayedRequests;
        if (filter != null && !filter.isEmpty()) {
            displayedRequests = maintenanceService.getFilteredRequests(filter);
        } else {
            displayedRequests = allRequests;
        }

        model.addAttribute("requests", displayedRequests);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inprogressCount", inprogressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("selectedStatus", filter);
        model.addAttribute("currentPage", "maintenance");

        return "maintenance-request-list";
    }

    // --- FORMULARIO DE REPORTE (CORREGIDO) ---
    @GetMapping("/maintenance-request-form/{facilityId}")
    public String showRequestForm(@PathVariable Integer facilityId, Model model) {
        Optional<Facility> facility = facilityService.getFacilityById(facilityId);
        if (facility.isPresent()) {
            MaintenanceRequest maintenanceRequest = new MaintenanceRequest();
            maintenanceRequest.setFacility(facility.get());

            // 1. Pasamos el objeto con el nombre estándar
            model.addAttribute("maintenanceRequest", maintenanceRequest);
            // 2. Pasamos el nombre para el banner
            model.addAttribute("facilityName", facility.get().getName());
            // 3. IMPORTANTE: Pasamos el ID suelto para el input hidden del HTML
            model.addAttribute("facilityId", facilityId);

            return "maintenance-request-form";
        } else {
            return "redirect:/facilities";
        }
    }

    // PROCESAR REPORTE (Sin cambios mayores, ya estaba bien)
    @PostMapping("/add")
    public String addRequest(@ModelAttribute MaintenanceRequest maintenanceRequest,
                             @RequestParam("facilityId") Integer facilityId,
                             Authentication authentication) {

        Facility facility = facilityService.getFacilityById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Facility ID"));
        maintenanceRequest.setFacility(facility);

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

        maintenanceRequest.setReportDate(LocalDateTime.now());
        maintenanceRequest.setStatus("PENDING");

        maintenanceService.createRequest(maintenanceRequest);

        if (!userEmail.isEmpty()) {
            String subject = "Maintenance Request Received: " + facility.getName();
            String body = "Hello " + userName + ",\n\n" +
                    "We have received your maintenance report for " + facility.getName() + ".\n" +
                    "Issue: " + maintenanceRequest.getIssueType() + "\n\n" +
                    "Our maintenance team will review it shortly.\n" +
                    "Thank you for helping us keep FitEasePWR in top shape!\n\n" +
                    "Best regards,\nFitEasePWR Team";
            // Descomenta si tienes el email service activo
            emailService.sendEmail(userEmail, subject, body);
        }

        return "redirect:/facilities";
    }

    @PostMapping("/status/{id}/{newStatus}")
    public String updateStatusFromUrl(@PathVariable Integer id, @PathVariable String newStatus) {
        String statusUpper = newStatus.replace("-", "_").toUpperCase();
        maintenanceService.updateRequestStatus(id, statusUpper);
        return "redirect:/maintenance-requests";
    }
}