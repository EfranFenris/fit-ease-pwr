package start.spring.io.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.repository.MaintenanceRequestRepository;

/**
 * Service layer for MaintenanceRequest business logic.
 * Handles CRUD operations and business rules.
 */
@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository repository;

    /** Injects the repository dependency. */
    public MaintenanceRequestService(MaintenanceRequestRepository repository) {
        this.repository = repository;
    }

    /** Get all maintenance requests. */
    public List<MaintenanceRequest> getAllRequests() {
        return repository.findAll();
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
