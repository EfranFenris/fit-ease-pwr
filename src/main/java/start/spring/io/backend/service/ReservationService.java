package start.spring.io.backend.service;

import org.springframework.stereotype.Service;
import start.spring.io.backend.model.Reservation;
import start.spring.io.backend.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    private final ReservationRepository repo;

    public ReservationService(ReservationRepository repo) {
        this.repo = repo;
    }

    public List<Reservation> getAll() {
        return repo.findAll();
    }

    public List<Reservation> getByUserId(Integer userId) {
        return repo.findByUserId(userId);
    }

    public boolean hasConflict(Reservation reservation, Integer excludeReservationId) {
        LocalDateTime startDateTime = reservation.getDate();
        LocalDateTime dayStart = startDateTime.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        LocalTime startTime = reservation.getStartTime();
        LocalTime endTime = reservation.getEndTime();
        return !repo.findConflicts(
                reservation.getFacilityId(),
                dayStart,
                dayEnd,
                startTime,
                endTime,
                excludeReservationId).isEmpty();
    }

    public Optional<Reservation> getById(Integer id) {
        return repo.findById(id);
    }

    public Reservation create(Reservation r) {
        r.setReservationId(null);
        return repo.save(r);
    }

    public Optional<Reservation> update(Integer id, Reservation details) {
        return repo.findById(id).map(r -> {
            r.setUserId(details.getUserId());
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
}
