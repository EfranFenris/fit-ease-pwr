package start.spring.io.backend.service;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import start.spring.io.backend.model.Facility;
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
    private final EmailService emailService;
    private final FacilityService facilityService;

    public ReservationService(ReservationRepository repo,
                              UserService userService,
                              EmailService emailService,
                              @Lazy FacilityService facilityService) {
        this.repo = repo;
        this.userService = userService;
        this.emailService = emailService;
        this.facilityService = facilityService;
    }

    public List<Reservation> getAll() { return repo.findAll(); }
    public List<Reservation> getByUserId(Integer userId) { return repo.findByUser_UserId(userId); }
    public Optional<Reservation> getById(Integer id) { return repo.findById(id); }

    // NUEVO CREATE: Acepta IDs y busca los objetos
    public Reservation create(Reservation r, Integer userId, Integer facilityId) {
        r.setReservationId(null);

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Facility facility = facilityService.getFacilityById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));

        r.setUser(user);
        r.setFacility(facility);

        return repo.save(r);
    }

    public Reservation create(Reservation r) { return repo.save(r); }

    public boolean hasOverlap(Integer facilityId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay().minusNanos(1);
        // Usamos el nuevo nombre del repo
        return repo.findByFacility_FacilityIdAndDateBetween(facilityId, dayStart, dayEnd).stream()
                .anyMatch(existing -> timesOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime()));
    }

    public boolean hasUserOverlap(Integer userId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay().minusNanos(1);
        return repo.findByUser_UserIdAndDateBetween(userId, dayStart, dayEnd).stream()
                .anyMatch(existing -> timesOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime()));
    }

    public Optional<Reservation> update(Integer id, Reservation details) {
        return repo.findById(id).map(r -> {
            if(details.getUser() != null) r.setUser(details.getUser());
            // Si viene facility nueva, la asignamos
            if(details.getFacility() != null) r.setFacility(details.getFacility());

            r.setDate(details.getDate());
            r.setStartTime(details.getStartTime());
            r.setEndTime(details.getEndTime());
            r.setParticipants(details.getParticipants());
            r.setPurpose(details.getPurpose());
            return repo.save(r);
        });
    }

    public void delete(Integer id) { repo.deleteById(id); }

    private boolean timesOverlap(LocalTime start, LocalTime end, LocalTime existingStart, LocalTime existingEnd) {
        return start.isBefore(existingEnd) && end.isAfter(existingStart);
    }

    public List<Reservation> getReservationsByDateRange(LocalDateTime start, LocalDateTime end) {
        return repo.findAllByDateBetween(start, end);
    }

    public void cancelReservationsForFacility(Integer facilityId, String reason) {
        // Usamos el nuevo nombre del repo
        List<Reservation> futureReservations = repo.findByFacility_FacilityIdAndDateAfter(facilityId, LocalDateTime.now());

        String facilityName = facilityService.getFacilityById(facilityId)
                .map(Facility::getName)
                .orElse("Sports Facility");

        for (Reservation r : futureReservations) {
            if (r.getUser() != null) {
                String userEmail = r.getUser().getEmail();
                String userName = r.getUser().getName();

                String subject = "⚠️ Booking Cancelled: " + facilityName;
                String body = "Dear " + userName + ",\n\n" +
                        "We regret to inform you that your reservation for " + facilityName +
                        " on " + r.getDate().toLocalDate() + " at " + r.getStartTime() +
                        " has been CANCELLED.\n\n" +
                        "Reason: " + reason + "\n\n" +
                        "FitEasePWR Team";
                emailService.sendEmail(userEmail, subject, body);
            }
            repo.delete(r);
        }
    }
}