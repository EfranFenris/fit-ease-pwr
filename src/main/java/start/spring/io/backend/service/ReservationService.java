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

/**
 * This service contains the business logic for bookings.
 */
@Service
public class ReservationService {

    private final ReservationRepository repo;
    private final UserService userService;
    private final EmailService emailService;
    private final FacilityService facilityService;

    /**
     * Constructor Injection.
     * @Lazy on FacilityService. Since FacilityService uses ReservationService, and ReservationService uses FacilityService,
     * they would normally crash waiting for each other, but it tells Spring: "Load FacilityService later, when we actually need it."
     */
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

    /**
     * Creates a reservation using ID numbers.
     * The web form sends us "User ID 5" and "Facility ID 2".
     * We convert those numbers into real Objects (User, Facility) before saving.
     */
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

    /**
     * It checks if a requested time slot overlaps with any existing booking for a facility.
     * Logic: Two events overlap if (StartA < EndB) and (EndA > StartB).
     */
    public boolean hasOverlap(Integer facilityId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay().minusNanos(1);

        // We get all reservations for that specific day and facility
        return repo.findByFacility_FacilityIdAndDateBetween(facilityId, dayStart, dayEnd).stream()
                // And check if our new time hits any of them
                .anyMatch(existing -> timesOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime()));
    }

    /**
     * Similar to the above, but checks for the User.
     * Prevents a user from booking two different games at the exact same time.
     */
    public boolean hasUserOverlap(Integer userId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay().minusNanos(1);
        return repo.findByUser_UserIdAndDateBetween(userId, dayStart, dayEnd).stream()
                .anyMatch(existing -> timesOverlap(startTime, endTime, existing.getStartTime(), existing.getEndTime()));
    }

    /**
     * Updates an existing reservation.
     * Allows changing time, date, or even moving it to a different court.
     */
    public Optional<Reservation> update(Integer id, Reservation details) {
        return repo.findById(id).map(r -> {
            if(details.getUser() != null) r.setUser(details.getUser());

            // If the user changed the facility, we update the link
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

    /**
     * Helper method to calculate time collision.
     * Returns TRUE if the times conflict.
     */
    private boolean timesOverlap(LocalTime start, LocalTime end, LocalTime existingStart, LocalTime existingEnd) {
        return start.isBefore(existingEnd) && end.isAfter(existingStart);
    }

    /**
     * Fetches all reservations between two dates.
     * Used by the Manager Dashboard to show today's agenda.
     */
    public List<Reservation> getReservationsByDateRange(LocalDateTime start, LocalDateTime end) {
        return repo.findAllByDateBetween(start, end);
    }

    /**
     * When a facility breaks (Maintenance Request), this method:
     * 1. Finds all FUTURE bookings for that court.
     * 2. Sends an apology email to every affected user.
     * 3. Deletes the bookings from the system.
     */
    public void cancelReservationsForFacility(Integer facilityId, String reason) {
        // Find only future reservations (we don't cancel past games)
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

                // Send the email
                emailService.sendEmail(userEmail, subject, body);
            }
            // Remove the booking
            repo.delete(r);
        }
    }
}