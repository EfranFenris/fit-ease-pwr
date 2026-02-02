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

    public List<MaintenanceRequest> getAllRequests() {
        return repository.findAll();
    }

    public List<MaintenanceRequest> getFilteredRequests(String status) {
        return repository.findFiltered(status);
    }

    public Optional<MaintenanceRequest> getRequestById(Integer id) {
        return repository.findById(id);
    }

    // --- MÉTODOS DE CREACIÓN ---

    // 1. Método antiguo (busca por IDs): Lo mantenemos por si acaso
    public MaintenanceRequest createRequest(MaintenanceRequest request, Integer userId, Integer facilityId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Facility facility = facilityService.getFacilityById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found: " + facilityId));

        request.setUser(user);
        request.setFacility(facility);
        request.setRequestId(null);
        return repository.save(request);
    }

    // 2. NUEVO MÉTODO (Sobrecarga): Para el nuevo controlador que ya trae el objeto montado
    public MaintenanceRequest createRequest(MaintenanceRequest request) {
        return repository.save(request);
    }

    // --- MÉTODOS DE ACTUALIZACIÓN ---

    // Este es el método que te daba ERROR en el controlador. Ahora lo añadimos e integramos tu lógica.
    @Transactional
    public void updateRequestStatus(Integer id, String status) {
        // Si el estado es "IN_PROGRESS", usamos tu lógica para cerrar la pista
        if ("IN_PROGRESS".equalsIgnoreCase(status)) {
            markInProgress(id);
        }
        // Si el estado es "RESOLVED", usamos tu lógica para abrir la pista
        else if ("RESOLVED".equalsIgnoreCase(status)) {
            markResolved(id);
        }
        // Para cualquier otro estado (ej: PENDING), solo actualizamos el texto
        else {
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
            // Tu lógica: Cerrar la pista automáticamente
            facilityService.updateStatus(request.getFacility().getFacilityId(), "Unavailable");
        });
    }

    @Transactional
    public void markResolved(Integer id) {
        repository.findById(id).ifPresent(request -> {
            request.setStatus("RESOLVED");
            repository.save(request);
            // Tu lógica: Abrir la pista automáticamente
            facilityService.updateStatus(request.getFacility().getFacilityId(), "Available");
        });
    }

    // --- OTROS MÉTODOS ---

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