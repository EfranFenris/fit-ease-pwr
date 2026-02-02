package start.spring.io.backend.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.repository.PenaltyRepository;

@Service
public class PenaltyService {

    private final PenaltyRepository repository;

    public PenaltyService(PenaltyRepository repository) {
        this.repository = repository;
    }

    public List<Penalty> getAllPenalties() {
        return repository.findAll();
    }

    public Optional<Penalty> getPenaltyById(Integer id) {
        return repository.findById(id);
    }

    public Penalty createPenalty(Penalty penalty) {
        penalty.setPenaltyId(null);
        return repository.save(penalty);
    }

    public Penalty updatePenalty(Integer id, Penalty penaltyDetails) {
        return repository.findById(id).map(penalty -> {
            // Actualizamos la relación si viene en el detalle
            if (penaltyDetails.getUser() != null) {
                penalty.setUser(penaltyDetails.getUser());
            }
            penalty.setDescription(penaltyDetails.getDescription());
            penalty.setDatehour(penaltyDetails.getDatehour());
            return repository.save(penalty);
        }).orElse(null);
    }

    public boolean deletePenalty(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}