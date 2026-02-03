package start.spring.io.backend.service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import start.spring.io.backend.model.Facility;
import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.MaintenanceRequestRepository;

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

    public MaintenanceRequest createRequest(MaintenanceRequest request, Integer userId, Integer facilityId) {
        User user = userService.getUserById(userId).orElseThrow();
        Facility facility = facilityService.getFacilityById(facilityId).orElseThrow();
        request.setUser(user);
        request.setFacility(facility);
        request.setRequestId(null);
        return repository.save(request);
    }

    public MaintenanceRequest createRequest(MaintenanceRequest request) { return repository.save(request); }

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

    @Transactional
    public void markInProgress(Integer id) {
        repository.findById(id).ifPresent(request -> {
            request.setStatus("IN_PROGRESS");
            repository.save(request);

            Integer facilityId = request.getFacility().getFacilityId();
            facilityService.updateStatus(facilityId, "Unavailable");

            // CANCELACIÓN MASIVA
            reservationService.cancelReservationsForFacility(facilityId, "Urgent maintenance: " + request.getIssueType());
        });
    }

    @Transactional
    public void markResolved(Integer id) {
        repository.findById(id).ifPresent(request -> {
            request.setStatus("RESOLVED");
            repository.save(request);
            facilityService.updateStatus(request.getFacility().getFacilityId(), "Available");
        });
    }

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