package start.spring.io.backend.service;

import org.springframework.stereotype.Service;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.repository.FacilityRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FacilityService {
    private final FacilityRepository repository;

    public FacilityService(FacilityRepository repository) {
        this.repository = repository;
    }

    public List<Facility> getAllFacilities() {
        return repository.findAll();
    }

    public Optional<Facility> getFacilityById(Integer id) {
        return repository.findById(id);
    }

    public Facility createFacility(Facility request) {
        request.setFacilityId(null);
        return repository.save(request);
    }

    public Optional<Facility> updateFacility(Integer id, Facility FacilityDetails) {
        return repository.findById(id).map(request -> {
            request.setFacilityId(FacilityDetails.getFacilityId());
            request.setName(FacilityDetails.getName());
            request.setType(FacilityDetails.getType());
            request.setStatus(FacilityDetails.getStatus());
            return repository.save(request);
        });
    }

    /**
     * Método auxiliar para cambiar solo el estado de la facility.
     * Usado por MaintenanceRequestService.
     */
    public void updateStatus(Integer facilityId, String newStatus) {
        repository.findById(facilityId).ifPresent(facility -> {
            facility.setStatus(newStatus);
            repository.save(facility);
        });
    }

    public boolean deleteFacility(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    /** * Devuelve la capacidad máxima basada en el tipo de facility.
     */
    /**
     * Devuelve la capacidad máxima basada en el tipo de facility.
     * Usa lógica "contains" para ser más flexible con los nombres.
     */
    public int getCapacityForType(String type) {
        if (type == null) return 8; // Valor por defecto

        String t = type.toLowerCase();

        if (t.contains("soccer") || t.contains("football")) return 22;
        if (t.contains("basketball")) return 10;
        if (t.contains("tennis") || t.contains("padel")) return 4;
        if (t.contains("badminton")) return 6;
        if (t.contains("ping") || t.contains("pong")) return 2;

        return 8; // Default
    }
}