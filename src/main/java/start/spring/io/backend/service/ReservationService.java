package start.spring.io.backend.service;

import org.springframework.stereotype.Service;
import start.spring.io.backend.model.Reservation;
import start.spring.io.backend.repository.ReservationRepository;

import java.time.LocalDate;
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

    public Optional<Reservation> getById(Integer id) {
        return repo.findById(id);
    }

    public Reservation create(Reservation r) {
        r.setReservationId(null);
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
        return repo.findByUserIdAndDateBetween(userId, dayStart, dayEnd).stream()
                .anyMatch(existing -> timesOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime()));
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

    private boolean timesOverlap(LocalTime start, LocalTime end, LocalTime existingStart, LocalTime existingEnd) {
        return start.isBefore(existingEnd) && end.isAfter(existingStart);
    }
}
