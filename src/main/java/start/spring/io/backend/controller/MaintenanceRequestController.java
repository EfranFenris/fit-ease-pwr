package start.spring.io.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.model.User;
import start.spring.io.backend.service.FacilityService;
import start.spring.io.backend.service.MaintenanceRequestService;
import start.spring.io.backend.service.UserService;

@Controller
@RequestMapping("/maintenance-requests")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;
    private final UserService userService;
    private final FacilityService facilityService;

    public MaintenanceRequestController(MaintenanceRequestService maintenanceRequestService,
                                        UserService userService,
                                        FacilityService facilityService) {
        this.maintenanceRequestService = maintenanceRequestService;
        this.userService = userService;
        this.facilityService = facilityService;
    }

    /** VISTA PRINCIPAL (Dashboard) */
    @GetMapping
    public String listMaintenanceRequests(@RequestParam(required = false) String status, Model model) {

        // 1. Obtener todas para los contadores
        List<MaintenanceRequest> allRequests = maintenanceRequestService.getAllRequests();

        long pendingCount = allRequests.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long inprogressCount = allRequests.stream().filter(r -> "IN_PROGRESS".equals(r.getStatus())).count();
        long resolvedCount = allRequests.stream().filter(r -> "RESOLVED".equals(r.getStatus())).count();

        // 2. Filtrar si es necesario
        List<MaintenanceRequest> requestsToShow;
        if (status != null && !status.isEmpty()) {
            requestsToShow = maintenanceRequestService.getFilteredRequests(status);
        } else {
            requestsToShow = allRequests;
        }

        // 3. Pasar a la vista
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inprogressCount", inprogressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("requests", requestsToShow); // Esto ahora es List<MaintenanceRequest>
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentPage", "maintenance");

        return "maintenance-request-list";
    }

    /** Mostrar formulario de creación */
    @GetMapping("/maintenance-request-form/{facilityId}")
    public String showMaintenanceRequestForm(@PathVariable Integer facilityId, Model model) {
        String facilityName = facilityService.getFacilityById(facilityId)
                .map(start.spring.io.backend.model.Facility::getName)
                .orElseThrow(() -> new IllegalArgumentException("Invalid facility Id"));

        model.addAttribute("facilityId", facilityId);
        model.addAttribute("newRequest", new MaintenanceRequest());
        model.addAttribute("facilityName", facilityName);
        return "maintenance-request-form";
    }

    /** Procesar formulario de creación */
    @PostMapping("/maintenance-request-form")
    public String saveMaintenanceRequestFromForm(
            @ModelAttribute("newRequest") MaintenanceRequest request,
            @RequestParam("facilityId") Integer facilityId, // Recibimos el ID explícitamente
            @RequestParam(defaultValue = "facilities") String from,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        request.setStatus("PENDING");
        request.setReportDate(LocalDateTime.now());

        // Obtener Usuario
        String email = authentication.getName();
        Integer userId = userService.getUserByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Guardar usando el nuevo método del servicio
        maintenanceRequestService.createRequest(request, userId, facilityId);

        return "redirect:/" + from;
    }

    // --- ACCIONES DE ESTADO ---

    @PostMapping("/status/{id}/in-progress")
    public String startWork(@PathVariable Integer id) {
        maintenanceRequestService.markInProgress(id);
        return "redirect:/maintenance-requests";
    }

    @PostMapping("/status/{id}/resolved")
    public String markResolved(@PathVariable Integer id) {
        maintenanceRequestService.markResolved(id);
        return "redirect:/maintenance-requests";
    }
}