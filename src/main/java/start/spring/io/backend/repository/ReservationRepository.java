package start.spring.io.backend.repository;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import start.spring.io.backend.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    List<Reservation> findByUser_UserId(Integer userId);

    List<Reservation> findByUser_UserIdAndDateBetween(Integer userId, LocalDateTime start, LocalDateTime end);

    // CAMBIO: Usamos Facility_FacilityId
    List<Reservation> findByFacility_FacilityIdAndDateBetween(Integer facilityId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findAllByDateBetween(LocalDateTime start, LocalDateTime end);

    // CAMBIO: Usamos Facility_FacilityId
    List<Reservation> findByFacility_FacilityIdAndDateAfter(Integer facilityId, LocalDateTime date);
}