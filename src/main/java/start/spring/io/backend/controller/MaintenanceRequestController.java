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

/**
 * This controller manages the Maintenance Requests.
 * It allows users to report broken equipment and allows staff to see
 * lists of issues and update their status (for example from "Pending" to "Fixed").
 */
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

    /**
     * Displays a list of maintenance requests.
     * It also calculates how many requests are Pending, In Progress, or Resolved
     * so we can show those numbers on the dashboard.
     * * @param filter An optional parameter (like "status=PENDING") to show only specific requests.
     */
    @GetMapping
    public String listRequests(Model model, @RequestParam(value = "status", required = false) String filter) {
        List<MaintenanceRequest> allRequests = maintenanceService.getAllRequests();

        // We count how many requests exist for each status to display sum label
        long pendingCount = allRequests.stream().filter(r -> "PENDING".equalsIgnoreCase(r.getStatus())).count();
        long inprogressCount = allRequests.stream().filter(r -> "IN_PROGRESS".equalsIgnoreCase(r.getStatus())).count();
        long resolvedCount = allRequests.stream().filter(r -> "RESOLVED".equalsIgnoreCase(r.getStatus())).count();

        List<MaintenanceRequest> displayedRequests;
        // If the user clicked a filter button (like "Show only Pending"), we filter the list.
        // Otherwise, we show everything.
        if (filter != null && !filter.isEmpty()) {
            displayedRequests = maintenanceService.getFilteredRequests(filter);
        } else {
            displayedRequests = allRequests;
        }

        // Pass all the data to the HTML view
        model.addAttribute("requests", displayedRequests);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inprogressCount", inprogressCount);
        model.addAttribute("resolvedCount", resolvedCount);
        model.addAttribute("selectedStatus", filter);
        model.addAttribute("currentPage", "maintenance");

        return "maintenance-request-list";
    }

    /**
     * Shows the form to report a problem with a specific facility.
     * We need the 'facilityId' so we know exactly which court or field has the issue.
     */
    @GetMapping("/maintenance-request-form/{facilityId}")
    public String showRequestForm(@PathVariable Integer facilityId, Model model) {
        Optional<Facility> facility = facilityService.getFacilityById(facilityId);
        // We only show the form if the facility actually exists in our database
        if (facility.isPresent()) {
            MaintenanceRequest maintenanceRequest = new MaintenanceRequest();
            maintenanceRequest.setFacility(facility.get());

            // We pass the empty object to be filled by the form
            model.addAttribute("maintenanceRequest", maintenanceRequest);
            // We pass the name so the user knows what they are reporting (for example "Broken net in Tennis Court 1")
            model.addAttribute("facilityName", facility.get().getName());
            // We pass the ID separately to keep it in a hidden input field
            model.addAttribute("facilityId", facilityId);

            return "maintenance-request-form";
        } else {
            // If the ID is wrong, go back to the main list
            return "redirect:/facilities";
        }
    }

    /**
     * Processes the submitted form.
     * It saves the new report, links it to the logged-in user, and sends a confirmation email.
     */
    @PostMapping("/add")
    public String addRequest(@ModelAttribute MaintenanceRequest maintenanceRequest,
                             @RequestParam("facilityId") Integer facilityId,
                             Authentication authentication) {

        // Find the facility again
        Facility facility = facilityService.getFacilityById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Facility ID"));
        maintenanceRequest.setFacility(facility);

        String userEmail = "";
        String userName = "User";

        // If the user is logged in, we link this report to their account
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.getUserByEmail(authentication.getName()).orElse(null);
            if (user != null) {
                maintenanceRequest.setUser(user);
                userEmail = user.getEmail();
                userName = user.getName();
            }
        }

        // Automatically set the date to now and status to "PENDING"
        maintenanceRequest.setReportDate(LocalDateTime.now());
        maintenanceRequest.setStatus("PENDING");

        // Save to database
        maintenanceService.createRequest(maintenanceRequest);

        // Send a confirmation email to the user if we have their address
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

        // Redirect back to the facilities page so they can continue browsing
        return "redirect:/facilities";
    }

    /**
     * Updates the status of a request quickly.
     * The new status comes directly from the URL.
     */
    @PostMapping("/status/{id}/{newStatus}")
    public String updateStatusFromUrl(@PathVariable Integer id, @PathVariable String newStatus) {
        // Convert format (e.g., "in-progress" -> "IN_PROGRESS") to match database standards
        String statusUpper = newStatus.replace("-", "_").toUpperCase();
        maintenanceService.updateRequestStatus(id, statusUpper);
        // Refresh the list page
        return "redirect:/maintenance-requests";
    }
}