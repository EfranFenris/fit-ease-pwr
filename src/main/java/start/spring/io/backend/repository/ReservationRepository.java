package start.spring.io.backend.repository;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import start.spring.io.backend.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByUserId(Integer userId);

    List<Reservation> findByFacilityIdAndDateBetween(Integer facilityId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findByUserIdAndDateBetween(Integer userId, LocalDateTime start, LocalDateTime end);
}
