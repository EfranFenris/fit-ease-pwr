package start.spring.io.backend.service;

import org.springframework.stereotype.Service;
import start.spring.io.backend.model.Reservation;
import start.spring.io.backend.repository.ReservationRepository;

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