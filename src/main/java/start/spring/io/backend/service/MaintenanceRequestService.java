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

    public MaintenanceRequestService(MaintenanceRequestRepository repository, UserService userService, FacilityService facilityService) {
        this.repository = repository;
        this.userService = userService;
        this.facilityService = facilityService;
    }

    /** Obtiene todas las requests (Hibernate ya trae User y Facility dentro) */
    public List<MaintenanceRequest> getAllRequests() {
        return repository.findAll();
    }

    /** Obtiene requests filtradas usando la query del repositorio */
    public List<MaintenanceRequest> getFilteredRequests(String status) {
        return repository.findFiltered(status);
    }

    public Optional<MaintenanceRequest> getRequestById(Integer id) {
        return repository.findById(id);
    }

    /** * Crea una request buscando las entidades User y Facility por sus IDs.
     */
    public MaintenanceRequest createRequest(MaintenanceRequest request, Integer userId, Integer facilityId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Facility facility = facilityService.getFacilityById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found: " + facilityId));

        request.setUser(user);
        request.setFacility(facility);
        request.setRequestId(null); // Asegurar que es nuevo

        return repository.save(request);
    }

    /** Actualiza una request existente */
    public Optional<MaintenanceRequest> updateRequest(Integer id, MaintenanceRequest requestDetails) {
        return repository.findById(id).map(request -> {
            // Nota: Aquí podrías añadir lógica para actualizar user/facility si fuera necesario
            if(requestDetails.getUser() != null) request.setUser(requestDetails.getUser());
            if(requestDetails.getFacility() != null) request.setFacility(requestDetails.getFacility());

            request.setDescription(requestDetails.getDescription());
            request.setStatus(requestDetails.getStatus());
            request.setReportDate(requestDetails.getReportDate());
            request.setIssueType(requestDetails.getIssueType());
            request.setSeverity(requestDetails.getSeverity());
            return repository.save(request);
        });
    }

    @Transactional
    public void markInProgress(Integer id) {
        repository.findById(id).ifPresent(request -> {
            request.setStatus("IN_PROGRESS");
            repository.save(request);
            // Accedemos al ID a través del objeto Facility
            facilityService.updateStatus(request.getFacility().getFacilityId(), "Unavailable");
        });
    }

    @Transactional
    public void markResolved(Integer id) {
        repository.findById(id).ifPresent(request -> {
            request.setStatus("RESOLVED");
            repository.save(request);
            // Accedemos al ID a través del objeto Facility
            facilityService.updateStatus(request.getFacility().getFacilityId(), "Available");
        });
    }

    public boolean deleteRequest(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}