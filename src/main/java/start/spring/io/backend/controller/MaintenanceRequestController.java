package start.spring.io.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.service.FacilityService;
import start.spring.io.backend.service.MaintenanceRequestService;
import start.spring.io.backend.service.UserService;

/**
 * Controller for MaintenanceRequest CRUD.
 * Provides both HTML views and REST API endpoints.
 */
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

    /** Display list of all maintenance requests with form to add new. */
    @GetMapping
    public String listMaintenanceRequests(Model model) {
        List<start.spring.io.backend.model.User> users = userService.getAllUsers();
        List<start.spring.io.backend.model.User> maintenanceUsers = users.stream()
                .filter(u -> "maintenance".equalsIgnoreCase(u.getRole()))
                .toList();
        List<start.spring.io.backend.model.Facility> facilities = facilityService.getAllFacilities();

        Map<Integer, String> userNames = users.stream()
                .collect(Collectors.toMap(start.spring.io.backend.model.User::getUserId,
                        start.spring.io.backend.model.User::getName));
        Map<Integer, String> facilityNames = facilities.stream()
                .collect(Collectors.toMap(start.spring.io.backend.model.Facility::getFacilityId,
                        start.spring.io.backend.model.Facility::getName));

        model.addAttribute("maintenanceRequests", maintenanceRequestService.getAllRequests());
        model.addAttribute("users", users);
        model.addAttribute("maintenanceUsers", maintenanceUsers);
        model.addAttribute("facilities", facilities);
        model.addAttribute("userNames", userNames);
        model.addAttribute("facilityNames", facilityNames);
        model.addAttribute("newRequest", new MaintenanceRequest());
        return "maintenance-request-list";
    }

    /** Add new maintenance request. */
    @PostMapping("/add")
    public String addMaintenanceRequest(@ModelAttribute("newRequest") MaintenanceRequest request) {
        maintenanceRequestService.createRequest(request);
        return "redirect:/maintenance-requests";
    }

    /** Show edit form for maintenance request. */
    @GetMapping("/edit/{id}")
    public String editMaintenanceRequestForm(@PathVariable Integer id, Model model) {
        MaintenanceRequest request = maintenanceRequestService.getRequestById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid maintenance request Id: " + id));
        List<start.spring.io.backend.model.User> users = userService.getAllUsers();
        List<start.spring.io.backend.model.User> maintenanceUsers = users.stream()
                .filter(u -> "maintenance".equalsIgnoreCase(u.getRole()))
                .toList();
        model.addAttribute("request", request);
        model.addAttribute("users", users);
        model.addAttribute("maintenanceUsers", maintenanceUsers);
        model.addAttribute("facilities", facilityService.getAllFacilities());
        return "maintenance-request-edit";
    }

    /** Update maintenance request. */
    @PostMapping("/edit/{id}")
    public String editMaintenanceRequest(@PathVariable Integer id,
            @ModelAttribute("request") MaintenanceRequest request) {
        maintenanceRequestService.updateRequest(id, request);
        return "redirect:/maintenance-requests";
    }

    /** Delete maintenance request. */
    @GetMapping("/delete/{id}")
    public String deleteMaintenanceRequest(@PathVariable Integer id) {
        maintenanceRequestService.deleteRequest(id);
        return "redirect:/maintenance-requests";
    }

    /** Show form to create a new maintenance request. */
    @GetMapping("/maintenance-request-form/{facilityId}")
    public String showMaintenanceRequestForm(@PathVariable Integer facilityId, Model model) {
        /**
         * (Using session from HTTPSession as a parameter)
         * Integer userID = (Integer) session.getAttribute("loggedInUserId");
         * 
         * if (userID == null) {
         * return "redirect:/login"; // Redirect to login if user is not logged in
         * }
         */

        model.addAttribute("facilityId", facilityId);
        model.addAttribute("newRequest", new MaintenanceRequest());
        model.addAttribute("facilityName", facilityService.getFacilityById(facilityId)
                .map(start.spring.io.backend.model.Facility::getName)
                .orElse("Unknown Facility"));
        return "maintenance-request-form";
    }

    /** Save new maintenance request from form. */
    @PostMapping("/maintenance-request-form")
    public String saveMaintenanceRequestFromForm(@ModelAttribute("newRequest") MaintenanceRequest request,
            @RequestParam(defaultValue = "facilities") String from) {
        request.setStatus("PENDING");
        request.setReportDate(java.time.LocalDateTime.now());
        request.setUserId(1); // Temporary hardcoded user ID
        maintenanceRequestService.createRequest(request);
        return "redirect:/" + from;
    }

    /** Dashboard view for maintance requests */
    @GetMapping("/dashboard")
    public String maintenanceDashboard(
            @RequestParam(required = false) String status,
            Model model) {

        List<MaintenanceRequest> requests;

        if (status != null && !status.isEmpty()) {
            requests = maintenanceRequestService.getRequestsByStatus(status);
        } else {
            requests = maintenanceRequestService.getAllRequests();
        }

        model.addAttribute("pendingCount", requests.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
        model.addAttribute("inprogressCount", requests.stream().filter(r -> "IN_PROGRESS".equals(r.getStatus())).count());
        model.addAttribute("resolvedCount", requests.stream().filter(r -> "CLOSED".equals(r.getStatus())).count());

        model.addAttribute("requests", requests);
        model.addAttribute("selectedStatus", status);

        return "maintenance-requests-dashboard";
    }
}
