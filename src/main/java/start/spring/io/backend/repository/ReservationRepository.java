package start.spring.io.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import start.spring.io.backend.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
}