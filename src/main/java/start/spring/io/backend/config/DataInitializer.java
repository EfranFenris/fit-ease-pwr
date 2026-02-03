package start.spring.io.backend.config;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import start.spring.io.backend.model.Facility;
import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.model.Reservation;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.FacilityRepository;
import start.spring.io.backend.repository.MaintenanceRequestRepository;
import start.spring.io.backend.repository.PenaltyRepository;
import start.spring.io.backend.repository.ReservationRepository;
import start.spring.io.backend.repository.UserRepository;

/**
 * This class is responsible for loading initial test data into the database when the application starts.
 * It implements CommandLineRunner, which means the 'run' method will execute automatically
 * after the server starts up. This ensures we don't have an empty database while testing.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    // These repositories are the tools we use to save data to the database tables.
    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository;
    private final MaintenanceRequestRepository maintenanceRepository;
    private final ReservationRepository reservationRepository;

    /**
     * Constructor injection: Spring automatically provides the repositories we need
     * when creating this component.
     */
    public DataInitializer(
            FacilityRepository facilityRepository,
            UserRepository userRepository,
            PenaltyRepository penaltyRepository,
            MaintenanceRequestRepository maintenanceRepository,
            ReservationRepository reservationRepository
    ) {
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.penaltyRepository = penaltyRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * This method runs automatically at startup.
     * We use "if" checks to make sure we don't create duplicate data if the app restarts.
     */
    @Override
    public void run(String... args) throws Exception {

        // Create sports facilities (Courts/Fields) if the table is currently empty.
        if (facilityRepository.count() == 0) {
            facilityRepository.save(new Facility("Tennis Court 1", "Tennis", "Available"));
            facilityRepository.save(new Facility("Badminton Court 1", "Badminton", "Unavailable"));
            facilityRepository.save(new Facility("Ping Pong 1", "Ping Pong", "Available"));
            facilityRepository.save(new Facility("Padel Court 1", "Padel", "Available"));
            facilityRepository.save(new Facility("Soccer Field Main", "Football", "Available"));
            facilityRepository.save(new Facility("Basketball Court", "Basketball", "Available"));
        }

        // Create default users for testing purposes.
        // The long strings (e.g., "$2a$12$...") are the passwords encrypted with BCrypt.

        // Create an Admin user
        if (!userRepository.findByEmail("admin@test.com").isPresent()) {
            userRepository.save(new User("Reservation Manager", "admin@test.com", "$2a$12$fhX51HXW8X7YwQKQVLUep.D457PdylfzXn2TeGH.wMuZljIDMdnze", "admin"));
        }
        // Create a Regular User
        if (!userRepository.findByEmail("user@test.com").isPresent()) {
            userRepository.save(new User("Regular User", "user@test.com", "$2a$12$MNyGGaKA5F.QPaCENXm5pOT.qyaMhI.AqAwpgTCSSLjWLbN/xpXaS", "user"));
        }
        // Create a Maintenance Staff user
        if (!userRepository.findByEmail("manteinance@test.com").isPresent()) {
            userRepository.save(new User("Manteinance Staff", "manteinance@test.com", "$2a$12$IdWu/vuRLHEkHfKLXOPjReiWUcFh7QHOGNAbNM0FPvlTdWGj945fi", "maintenance"));
        }
        // Create a specific user named "Marta" to link with reservations and penalties below
        User marta;
        if (!userRepository.findByEmail("marta@test.com").isPresent()) {
            marta = userRepository.save(new User("Marta", "marta@test.com", "$2a$12$RySmtaQLiW6cxrdt/fsqn.PsTTedmS1ieJ7pGgVpjegeyP8Ddb.MS", "user"));
        } else {
            marta = userRepository.findByEmail("marta@test.com").get();
        }

        // Create a test Reservation for Marta on the Padel court.
        // We search for the Padel facility first, then assign it to the reservation.
        if (reservationRepository.count() == 0) {
            Facility padel = facilityRepository.findAll().stream()
                    .filter(f -> f.getType().equals("Padel"))
                    .findFirst().orElse(null);

            if (padel != null && marta != null) {
                Reservation r = new Reservation();
                r.setFacility(padel);
                r.setUser(marta);
                r.setDate(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
                r.setStartTime(LocalTime.of(10, 0));
                r.setEndTime(LocalTime.of(11, 30));
                r.setParticipants(4);
                r.setPurpose("Training match");

                reservationRepository.save(r);
            }
        }

        // Assign a Penalty to Marta to test the penalty system.
        // This simulates that she missed a reservation yesterday.
        if (marta != null && penaltyRepository.count() == 0) {
            penaltyRepository.save(new Penalty(marta, "Did not show up without canceling", LocalDateTime.now().minusDays(1)));
        }

        // Create a Maintenance Request for the Badminton court.
        // This simulates a user reporting broken lights.
        if (marta != null && maintenanceRepository.count() == 0) {
            Facility badminton = facilityRepository.findAll().stream()
                    .filter(f -> f.getName().contains("Badminton"))
                    .findFirst()
                    .orElse(null);

            if (badminton != null) {
                MaintenanceRequest req = new MaintenanceRequest();
                req.setUser(marta);
                req.setFacility(badminton);
                req.setIssueType("Lighting Failure");
                req.setDescription("The main lights are flickering.");
                req.setSeverity("HIGH");
                req.setStatus("IN_PROGRESS");
                req.setReportDate(LocalDateTime.now().minusHours(4));

                maintenanceRepository.save(req);
            }
        }
    }
}