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
        User maria = null;
        User miryam = null;

        // 1. Facilities
        if (facilityRepository.count() == 0) {
            facilityRepository.save(new Facility("Tennis-Court-1", "Tennis", "Available"));
            facilityRepository.save(new Facility("Badminton-Court-1", "Badminton", "Unavailable"));
            facilityRepository.save(new Facility("Ping-Pong-1", "Ping Pong", "Available"));
            facilityRepository.save(new Facility("Padel-Court-1", "Padel", "Available"));
            facilityRepository.save(new Facility("Soccer-Field-Main", "Football", "Available"));
        }

        // 2. Users
        if (!userRepository.findByEmail("juan@example.com").isPresent()) {
            userRepository.save(new User("Juan Perez", "juan@example.com", "123456", "admin"));
        }

        if (!userRepository.findByEmail("maria@example.com").isPresent()) {
            maria = userRepository.save(new User("Maria Lopez", "maria@example.com", "password", "user"));
        } else {
            maria = userRepository.findByEmail("maria@example.com").orElse(null);
        }

        if (!userRepository.findByEmail("miryam@example.com").isPresent()) {
            miryam = userRepository.save(new User("Miryam Merchan", "miryam@example.com", "charlie", "maintenance"));
        } else {
            miryam = userRepository.findByEmail("miryam@example.com").orElse(null);
        }

        if (!userRepository.findByEmail("carmen@example.com").isPresent()) {
            userRepository.save(new User("Carmen", "carmen@example.com", "uuuu", "user"));
        }

        // 3. Penalty
        if (maria != null && penaltyRepository.count() == 0) {
            penaltyRepository.save(new Penalty(maria.getUserId(), "Did not show up without canceling", LocalDateTime.now().minusDays(1)));
        }

        // 4. Maintenance Request (CORREGIDO PARA JPA)
        if (miryam != null && maintenanceRepository.count() == 0) {
            // Buscamos la pista por nombre
            Facility badminton = facilityRepository.findAll().stream()
                    .filter(f -> f.getName().contains("Badminton"))
                    .findFirst()
                    .orElse(null);

            if (badminton != null) {
                MaintenanceRequest req = new MaintenanceRequest();

                // CAMBIO IMPORTANTE: Pasamos los OBJETOS, no los IDs
                req.setUser(miryam);
                req.setFacility(badminton);

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