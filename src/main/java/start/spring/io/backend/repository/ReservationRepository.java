package start.spring.io.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import start.spring.io.backend.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByUserId(Integer userId);
}
