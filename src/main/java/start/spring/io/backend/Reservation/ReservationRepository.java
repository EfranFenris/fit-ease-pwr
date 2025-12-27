package start.spring.io.backend.Reservation;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for the Reservation entity.
 * Provides basic CRUD operations.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
}