package start.spring.io.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This repository manages the Strikes given to users.
 */
public interface PenaltyRepository extends JpaRepository<Penalty, Integer> {

    /**
     * A cleanup tool. It deletes all penalties that happened before a certain date.
     */
    @Transactional
    void deleteByDatehourBefore(LocalDateTime cutoffDate);

    /**
     * Finds all penalties assigned to a specific user.
     */
    List<Penalty> findByUser(User user);

    /**
     * Checks if a user has already been punished for a specific reason.
     * This prevents us from accidentally clicking the "Penalty" button twice
     * and giving the user two strikes for the same mistake.
     */
    boolean existsByUserAndDescription(User user, String description);
}