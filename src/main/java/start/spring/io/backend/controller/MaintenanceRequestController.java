package start.spring.io.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.service.MaintenanceRequestService;
import start.spring.io.backend.service.UserService;
import start.spring.io.backend.service.FacilityService;

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
            .collect(Collectors.toMap(start.spring.io.backend.model.User::getUserId, start.spring.io.backend.model.User::getName));
        Map<Integer, String> facilityNames = facilities.stream()
            .collect(Collectors.toMap(start.spring.io.backend.model.Facility::getFacilityId, start.spring.io.backend.model.Facility::getName));

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
}
