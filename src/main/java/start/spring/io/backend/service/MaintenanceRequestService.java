package start.spring.io.backend.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import start.spring.io.backend.dto.MaintenanceRequestDTO;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.MaintenanceRequestRepository;

/**
 * Service layer for MaintenanceRequest business logic.
 * Handles CRUD operations and business rules.
 */
@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository repository;
    private final UserService userService;
    private final FacilityService facilityService;

    /** Injects the repository dependency. */
    public MaintenanceRequestService(MaintenanceRequestRepository repository, UserService userService, FacilityService facilityService) {
        this.repository = repository;
        this.userService = userService;
        this.facilityService = facilityService;
    }

    /** Get all maintenance requests. */
    public List<MaintenanceRequest> getAllRequests() {
        return repository.findAll();
    }

    /** Get all maintenance requests with details. */
    public List<MaintenanceRequestDTO> getAllRequestsWithDetails(String status) {
        // Fetch already filtered and ordered results at the DB level for better performance
        List<MaintenanceRequest> requests = repository.findFiltered(status);
            
        return requests.stream()
            .map(r -> {
                MaintenanceRequestDTO dto = new MaintenanceRequestDTO();
                dto.setRequestId(r.getRequestId());
                dto.setFacilityName(facilityService.getFacilityById(r.getFacilityId())
                    .map(Facility::getName).orElse("Unknown"));
                dto.setUserName(userService.getUserById(r.getUserId())
                    .map(User::getName).orElse("Unknown"));
                dto.setUserEmail(userService.getUserById(r.getUserId())
                    .map(User::getEmail).orElse("Unknown"));
                dto.setFacilityId(r.getFacilityId());
                dto.setUserId(r.getUserId());
                dto.setIssueType(r.getIssueType());
                dto.setDescription(r.getDescription());
                dto.setSeverity(r.getSeverity());
                dto.setStatus(r.getStatus());
                dto.setReportDate(r.getReportDate());
                return dto;
            })
            .collect(Collectors.toList());
    }

    /** Get a maintenance request by id. */
    public Optional<MaintenanceRequest> getRequestById(Integer id) {
        return repository.findById(id);
    }

    /** Get maintenance requests by status. */
    public List<MaintenanceRequest> getRequestsByStatus(String status) {
        return repository.findByStatus(status);
    }

    /** Create a new maintenance request. */
    public MaintenanceRequest createRequest(MaintenanceRequest request) {
        request.setRequestId(null);
        return repository.save(request);
    }

    /** Update an existing maintenance request. */
    public Optional<MaintenanceRequest> updateRequest(Integer id, MaintenanceRequest requestDetails) {
        return repository.findById(id).map(request -> {
            request.setUserId(requestDetails.getUserId());
            request.setFacilityId(requestDetails.getFacilityId());
            request.setStaffId(requestDetails.getStaffId());
            request.setDescription(requestDetails.getDescription());
            request.setStatus(requestDetails.getStatus());
            request.setReportDate(requestDetails.getReportDate());
            request.setIssueType(requestDetails.getIssueType());
            request.setSeverity(requestDetails.getSeverity());
            return repository.save(request);
        });
    }

    /** Delete a maintenance request by id. */
    public boolean deleteRequest(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
