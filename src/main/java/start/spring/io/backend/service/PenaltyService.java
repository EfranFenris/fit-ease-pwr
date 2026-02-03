package start.spring.io.backend.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.PenaltyRepository;

/**
 * This service handles the business logic for Penalties.
 * It acts as the middleman between the Controller (which receives user input)
 * and the Repository (which saves data to the database).
 */
@Service
public class PenaltyService {

    private final PenaltyRepository repository;

    public PenaltyService(PenaltyRepository repository) {
        this.repository = repository;
    }

    /**
     * Retrieves a list of all penalties ever recorded.
     */
    public List<Penalty> getAllPenalties() {
        return repository.findAll();
    }

    /**
     * Finds a specific penalty by its unique ID number.
     */
    public Optional<Penalty> getPenaltyById(Integer id) {
        return repository.findById(id);
    }

    /**
     * Saves a new penalty to the database.
     * We ensure the ID is null so the database knows to create a new row,
     * rather than updating an old one.
     */
    public Penalty createPenalty(Penalty penalty) {
        penalty.setPenaltyId(null);
        return repository.save(penalty);
    }

    /**
     * Updates an existing penalty.
     * It looks for the penalty by ID. If found, it updates the details (User, Description, Date)
     * and saves it again. If not found, it returns null.
     */
    public Penalty updatePenalty(Integer id, Penalty penaltyDetails) {
        return repository.findById(id).map(penalty -> {
            // Update the relationship only if a new user is provided
            if (penaltyDetails.getUser() != null) {
                penalty.setUser(penaltyDetails.getUser());
            }
            penalty.setDescription(penaltyDetails.getDescription());
            penalty.setDatehour(penaltyDetails.getDatehour());
            return repository.save(penalty);
        }).orElse(null);
    }

    /**
     * Deletes a penalty permanently.
     * Returns true if successful, false if the penalty didn't exist.
     */
    public boolean deletePenalty(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Checks if a user already has a penalty for a specific reason.
     * This helps prevent duplicate punishments for the same mistake.
     */
    public boolean existsPenalty(User user, String description) {
        return repository.existsByUserAndDescription(user, description);
    }
}