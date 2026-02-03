package start.spring.io.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface PenaltyRepository extends JpaRepository<Penalty, Integer> {

    @Transactional
    void deleteByDatehourBefore(LocalDateTime cutoffDate);

    List<Penalty> findByUser(User user);

    // --- NUEVO: Método para comprobar duplicados ---
    boolean existsByUserAndDescription(User user, String description);
}