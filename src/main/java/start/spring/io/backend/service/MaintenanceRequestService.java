package start.spring.io.backend.service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import start.spring.io.backend.model.Facility;
import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.MaintenanceRequestRepository;

/**
 * This service manages the cycle of a maintenance issue.
 * It is a coordinator service because it talks to other services (Facility, Reservation)
 * to ensure that when something breaks, the court is closed and bookings are cancelled.
 */
@Service
public class MaintenanceRequestService {

    private final MaintenanceRequestRepository repository;
    private final UserService userService;
    private final FacilityService facilityService;
    private final ReservationService reservationService; // Inyectado

    public MaintenanceRequestService(MaintenanceRequestRepository repository,
                                     UserService userService,
                                     FacilityService facilityService,
                                     ReservationService reservationService) {
        this.repository = repository;
        this.userService = userService;
        this.facilityService = facilityService;
        this.reservationService = reservationService;
    }

    public List<MaintenanceRequest> getAllRequests() { return repository.findAll(); }
    public List<MaintenanceRequest> getFilteredRequests(String status) { return repository.findFiltered(status); }
    public Optional<MaintenanceRequest> getRequestById(Integer id) { return repository.findById(id); }

    /**
     * Creates a new report using ID numbers.
     */
    public MaintenanceRequest createRequest(MaintenanceRequest request, Integer userId, Integer facilityId) {
        User user = userService.getUserById(userId).orElseThrow();
        Facility facility = facilityService.getFacilityById(facilityId).orElseThrow();
        request.setUser(user);
        request.setFacility(facility);
        request.setRequestId(null);
        return repository.save(request);
    }

    /**
     * Creates a request when we already have the full User and Facility objects.
     */
    public MaintenanceRequest createRequest(MaintenanceRequest request) { return repository.save(request); }

    /**
     * Updates the status.
     * If the status changes to "IN_PROGRESS" or "RESOLVED", we trigger special actions.
     */
    @Transactional
    public void updateRequestStatus(Integer id, String status) {
        if ("IN_PROGRESS".equalsIgnoreCase(status)) {
            markInProgress(id);
        } else if ("RESOLVED".equalsIgnoreCase(status)) {
            markResolved(id);
        } else {
            repository.findById(id).ifPresent(request -> {
                request.setStatus(status);
                repository.save(request);
            });
        }
    }

    /**
     * Marks a request as "In Progress".
     * This automatically:
     * 1. Sets the facility status to "Unavailable".
     * 2. Cancels all future reservations for this court.
     */
    @Transactional
    public void markInProgress(Integer id) {
        repository.findById(id).ifPresent(request -> {
            request.setStatus("IN_PROGRESS");
            repository.save(request);

            Integer facilityId = request.getFacility().getFacilityId();
            facilityService.updateStatus(facilityId, "Unavailable");

            // MASS CANCELLATION: Notify users their game is off.
            reservationService.cancelReservationsForFacility(facilityId, "Urgent maintenance: " + request.getIssueType());
        });
    }

    /**
     * Marks a request as "Resolved" (Fixed).
     * This automatically re-opens the facility ("Available").
     */
    @Transactional
    public void markResolved(Integer id) {
        repository.findById(id).ifPresent(request -> {
            request.setStatus("RESOLVED");
            repository.save(request);
            facilityService.updateStatus(request.getFacility().getFacilityId(), "Available");
        });
    }

    /**
     * Checks if a facility currently has any UNRESOLVED issues.
     * Used to show the warning icon on the dashboard.
     */
    public boolean hasActiveRequests(Integer facilityId) {
        return repository.existsByFacility_FacilityIdAndStatusNot(facilityId, "RESOLVED");
    }

    public boolean deleteRequest(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}