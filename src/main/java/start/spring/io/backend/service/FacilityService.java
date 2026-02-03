package start.spring.io.backend.service;

import org.springframework.stereotype.Service;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.repository.FacilityRepository;

import java.util.List;
import java.util.Optional;

/**
 * This service manages the Sports Facilities (Courts and Fields).
 * It handles logic for creating, updating, and checking capacity of courts.
 */
@Service
public class FacilityService {
    private final FacilityRepository repository;

    public FacilityService(FacilityRepository repository) {
        this.repository = repository;
    }

    /**
     * Get a list of all facilities.
     */
    public List<Facility> getAllFacilities() {
        return repository.findAll();
    }

    /**
     * Find a specific facility by its ID number.
     */
    public Optional<Facility> getFacilityById(Integer id) {
        return repository.findById(id);
    }

    /**
     * Create a new facility.
     * We force the ID to null so the database knows it's a new item, not an update.
     */
    public Facility createFacility(Facility request) {
        request.setFacilityId(null);
        return repository.save(request);
    }

    /**
     * Update an existing facility's details.
     * We first check if it exists (findById), then update the fields, and save.
     */
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
     * Helper method to change ONLY the status (example: from "Available" to "Unavailable").
     * This is used by the MaintenanceService when a court breaks down.
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

    /**
     * Calculates the maximum number of players allowed based on the sport type.
     */
    public int getCapacityForType(String type) {
        if (type == null) return 8; // Default value

        String t = type.toLowerCase();

        if (t.contains("soccer") || t.contains("football")) return 22; // 11 vs 11
        if (t.contains("basketball")) return 10; // 5 vs 5
        if (t.contains("tennis") || t.contains("padel")) return 4;
        if (t.contains("badminton")) return 6;
        if (t.contains("ping") || t.contains("pong")) return 2;

        return 8; // Default number of players
    }
}