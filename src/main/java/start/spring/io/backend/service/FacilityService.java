package start.spring.io.backend.service;

import org.springframework.stereotype.Service;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.repository.FacilityRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FacilityService {
    private final FacilityRepository repository;

    /** Injects the repository dependency. */
    public FacilityService(FacilityRepository repository) {
        this.repository = repository;
    }

    /** Get all Facilities. */
    public List<Facility> getAllFacilities() {
        return repository.findAll();
    }

    /** Get a Facility by id. */
    public Optional<Facility> getFacilityById(Integer id) {
        return repository.findById(id);
    }

    /** Create a new Facility. */
    public Facility createFacility(Facility request) {
        request.setFacilityId(null);
        return repository.save(request);
    }

    /** Update an existing Facility. */
    public Optional<Facility> updateFacility(Integer id, Facility FacilityDetails) {
        return repository.findById(id).map(request -> {
            request.setFacilityId(FacilityDetails.getFacilityId());
            request.setName(FacilityDetails.getName());
            request.setType(FacilityDetails.getType());
            request.setStatus(FacilityDetails.getStatus());
            return repository.save(request);
        });
    }

    /** Delete a Facility by id. */
    public boolean deleteFacility(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
