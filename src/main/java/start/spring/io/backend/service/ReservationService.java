package start.spring.io.backend.service;

import org.springframework.stereotype.Service;
import start.spring.io.backend.model.Reservation;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.ReservationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository repo;
    private final UserService userService;

    public ReservationService(ReservationRepository repo, UserService userService) {
        this.repo = repo;
        this.userService = userService;
    }

    public List<Reservation> getAll() {
        return repo.findAll();
    }

    public List<Reservation> getByUserId(Integer userId) {
        // Busca usando la relación con User
        return repo.findByUser_UserId(userId);
    }

    public Optional<Reservation> getById(Integer id) {
        return repo.findById(id);
    }

    /**
     * Crea una reserva asociando el usuario correspondiente.
     */
    public Reservation create(Reservation r, Integer userId) {
        r.setReservationId(null);

        // Buscamos el objeto User completo para asignarlo a la relación
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        r.setUser(user);

        return repo.save(r);
    }

    // Sobrecarga por si se pasa la reserva ya montada (uso interno)
    public Reservation create(Reservation r) {
        return repo.save(r);
    }

    public boolean hasOverlap(Integer facilityId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay().minusNanos(1);
        return repo.findByFacilityIdAndDateBetween(facilityId, dayStart, dayEnd).stream()
                .anyMatch(existing -> timesOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime()));
    }

    public boolean hasUserOverlap(Integer userId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay().minusNanos(1);
        // Busca usando la relación con User
        return repo.findByUser_UserIdAndDateBetween(userId, dayStart, dayEnd).stream()
                .anyMatch(existing -> timesOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime()));
    }

    public Optional<Reservation> update(Integer id, Reservation details) {
        return repo.findById(id).map(r -> {
            // Actualizamos solo los campos de la reserva, mantenemos user y facility si no cambian
            if(details.getUser() != null) r.setUser(details.getUser());

            r.setFacilityId(details.getFacilityId());
            r.setDate(details.getDate());
            r.setStartTime(details.getStartTime());
            r.setEndTime(details.getEndTime());
            r.setParticipants(details.getParticipants());
            r.setPurpose(details.getPurpose());
            return repo.save(r);
        });
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    private boolean timesOverlap(LocalTime start, LocalTime end, LocalTime existingStart, LocalTime existingEnd) {
        return start.isBefore(existingEnd) && end.isAfter(existingStart);
    }

    // Método para el Dashboard del Admin (Roll Call)
    public List<Reservation> getReservationsByDateRange(LocalDateTime start, LocalDateTime end) {
        return repo.findAllByDateBetween(start, end);
    }
}