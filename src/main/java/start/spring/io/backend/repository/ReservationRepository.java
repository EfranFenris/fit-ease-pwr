package start.spring.io.backend.repository;

import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import start.spring.io.backend.model.Reservation;

/**
 * This is the engine for Bookings.
 * It is used to check availability and prevent double-bookings.
 */
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    /**
     * Finds all reservations made by a specific user.
     * (Used for the "My Profile" or "My Bookings" page).
     */
    List<Reservation> findByUser_UserId(Integer userId);

    /**
     * Finds reservations for a user within a specific time range.
     * (Used to check if the user is trying to play two games at the same time).
     */
    List<Reservation> findByUser_UserIdAndDateBetween(Integer userId, LocalDateTime start, LocalDateTime end);

    /**
     * Finds reservations for a specific Facility within a time range.
     * (Used to check if the court is already occupied when someone tries to book).
     */
    List<Reservation> findByFacility_FacilityIdAndDateBetween(Integer facilityId, LocalDateTime start, LocalDateTime end);

    /**
     * Gets all reservations for a specific day.
     * (Used for the Manager Dashboard to see the daily schedule).
     */
    List<Reservation> findAllByDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Finds all FUTURE reservations for a specific facility.
     * (Used when we close a court for maintenance and need to know which upcoming bookings to cancel).
     */
    List<Reservation> findByFacility_FacilityIdAndDateAfter(Integer facilityId, LocalDateTime date);
}