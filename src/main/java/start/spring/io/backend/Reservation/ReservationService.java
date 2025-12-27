package start.spring.io.backend.Reservation;

import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

/**
 * Service layer for Reservation business logic.
 * Handles CRUD operations and business rules.
 */
@Service
public class ReservationService {

    private final ReservationRepository repository;

    /** Injects the repository dependency. */
    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
    }

    /** Get all reservations. */
    public List<Reservation> getAllReservations() {
        return repository.findAll();
    }

    /** Get a reservation by id. */
    public Optional<Reservation> getReservationById(Integer id) {
        return repository.findById(id);
    }

    /** Create a new reservation. */
    public Reservation createReservation(Reservation reservation) {
        reservation.setReservationId(null);
        return repository.save(reservation);
    }

    /** Update an existing reservation. */
    public Optional<Reservation> updateReservation(Integer id, Reservation reservationDetails) {
        return repository.findById(id).map(reservation -> {
            reservation.setUserId(reservationDetails.getUserId());
            reservation.setFacilityId(reservationDetails.getFacilityId());
            reservation.setDate(reservationDetails.getDate());
            reservation.setStartTime(reservationDetails.getStartTime());
            reservation.setEndTime(reservationDetails.getEndTime());
            reservation.setParticipants(reservationDetails.getParticipants());
            reservation.setPurpose(reservationDetails.getPurpose());
            return repository.save(reservation);
        });
    }

    /** Delete a reservation by id. */
    public boolean deleteReservation(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}