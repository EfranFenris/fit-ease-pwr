package start.spring.io.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.repository.PenaltyRepository;

@Service
public class PenaltyService {
    
    private final PenaltyRepository repository;

    /** Injects the repository dependency. */
    public PenaltyService(PenaltyRepository repository) {
        this.repository = repository;
    }

    /** Get all penalties. */
    public List<Penalty> getAllPenalties() {
        return repository.findAll();
    }

    /** Get a penalty by id. */
    public Optional<Penalty> getPenaltyById(Integer id) {
        return repository.findById(id);
    }

    /** Create a new penalty. */
    public Penalty createPenalty(Penalty penalty) {
        penalty.setPenaltyId(null);
        return repository.save(penalty);
    }

    /** Update an existing penalty. */
    public Penalty updatePenalty(Integer id, Penalty penaltyDetails) {
        return repository.findById(id).map(penalty -> {
            penalty.setPenaltyId(penaltyDetails.getPenaltyId());
            penalty.setUserId(penaltyDetails.getUserId());
            penalty.setDescription(penaltyDetails.getDescription());
            penalty.setDatehour(penaltyDetails.getDatehour());
            return repository.save(penalty);
        }).orElse(null);
    }

    /** Delete a penalty by id. */
    public boolean deletePenalty(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
