package start.spring.io.backend.config;

import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import start.spring.io.backend.model.Facility;
import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.FacilityRepository;
import start.spring.io.backend.repository.MaintenanceRequestRepository;
import start.spring.io.backend.repository.PenaltyRepository;
import start.spring.io.backend.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository;
    private final MaintenanceRequestRepository maintenanceRepository;

    public DataInitializer(
            FacilityRepository facilityRepository,
            UserRepository userRepository,
            PenaltyRepository penaltyRepository,
            MaintenanceRequestRepository maintenanceRepository
    ) {
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.penaltyRepository = penaltyRepository;
        this.maintenanceRepository = maintenanceRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        User marta = null;

        // 1. Facilities
        if (facilityRepository.count() == 0) {
            facilityRepository.save(new Facility("Tennis Court 1", "Tennis", "Available"));
            facilityRepository.save(new Facility("Badminton Court 1", "Badminton", "Unavailable"));
            facilityRepository.save(new Facility("Ping Pong 1", "Ping Pong", "Available"));
            facilityRepository.save(new Facility("Padel Court 1", "Padel", "Available"));
            facilityRepository.save(new Facility("Soccer Field Main", "Football", "Available"));
        }

        // 2. Users

        // ADMIN
        if (!userRepository.findByEmail("admin@test.com").isPresent()) {
            userRepository.save(new User("Reservation Manager", "admin@test.com", "1234", "admin"));
        }

        // USER NORMAL (CORREGIDO: tesst -> test)
        if (!userRepository.findByEmail("user@test.com").isPresent()) {
            userRepository.save(new User("Regular User", "user@test.com", "1234", "user"));
        }

        // MAINTENANCE
        if (!userRepository.findByEmail("manteinance@test.com").isPresent()) {
            userRepository.save(new User("Manteinance Staff", "manteinance@test.com", "1234", "maintenance"));
        }

        // MARTA (CORREGIDO: example.com -> test.com para que coincida)
        if (!userRepository.findByEmail("marta@test.com").isPresent()) {
            marta = userRepository.save(new User("Marta", "marta@test.com", "1234", "user"));
        } else {
            // Si ya existe, la recuperamos para usarla en las multas
            marta = userRepository.findByEmail("marta@test.com").orElse(null);
        }

        // 3. Penalty
        if (marta != null && penaltyRepository.count() == 0) {
            // Pasamos el OBJETO User, no el ID
            penaltyRepository.save(new Penalty(marta, "Did not show up without canceling", LocalDateTime.now().minusDays(1)));
        }

        // 4. Maintenance Request
        if (marta != null && maintenanceRepository.count() == 0) {
            Facility badminton = facilityRepository.findAll().stream()
                    .filter(f -> f.getName().contains("Badminton"))
                    .findFirst()
                    .orElse(null);

            if (badminton != null) {
                MaintenanceRequest req = new MaintenanceRequest();
                req.setUser(marta); // Pasamos objeto User
                req.setFacility(badminton); // Pasamos objeto Facility

                req.setIssueType("Lighting Failure");
                req.setDescription("The main lights are flickering and creating a hazard.");
                req.setSeverity("HIGH");
                req.setStatus("IN_PROGRESS");
                req.setReportDate(LocalDateTime.now().minusHours(4));

                maintenanceRepository.save(req);
            }
        }
    }
}