package start.spring.io.backend.Reservation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Simple REST controller for Reservation CRUD.
 * Basic endpoints to test create, read, update and delete.
 */
@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;

    /** Injects the service layer. */
    public ReservationController(ReservationService service) {
        this.service = service;
    }

    /** Lists all reservations. */
    @GetMapping(value = {"", "/"})
    public List<Reservation> getAll() {
        return service.getAllReservations();
    }

    /** Gets a reservation by id, returns 404 if not found. */
    @GetMapping("/{id}")
    public Reservation getOne(@PathVariable Integer id) {
        return service.getReservationById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
    }

    /** Creates a new reservation (201 Created). */
    @PostMapping(value = {"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public Reservation create(@RequestBody Reservation reservation) {
        return service.createReservation(reservation);
    }

    /** Updates an existing reservation by id, 404 if not found. */
    @PutMapping("/{id}")
    public Reservation update(@PathVariable Integer id, @RequestBody Reservation reservation) {
        return service.updateReservation(id, reservation)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found"));
    }

    /** Deletes a reservation by id (204 No Content), 404 if not found. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        if (!service.deleteReservation(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found");
        }
    }
}