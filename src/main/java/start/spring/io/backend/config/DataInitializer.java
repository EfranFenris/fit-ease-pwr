package start.spring.io.backend.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import start.spring.io.backend.model.Facility;
import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.FacilityRepository;
import start.spring.io.backend.repository.PenaltyRepository;
import start.spring.io.backend.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository; 

    public DataInitializer(
            FacilityRepository facilityRepository,
            UserRepository userRepository,
            PenaltyRepository penaltyRepository
    ) {
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.penaltyRepository = penaltyRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        User maria = null;

        if (facilityRepository.count() == 0) {  // Only add if table is empty
            facilityRepository.save(new Facility("Tennis-Court-1", "outdoors", "free"));
            facilityRepository.save(new Facility("Badminton-Court-1", "indoors", "Under-Maintenance"));
            facilityRepository.save(new Facility("Ping-Pong-1", "indoors","booked"));
            facilityRepository.save(new Facility("Padel-Court-1", "outdoors", "free"));
        }

        // Insert users only if they don't exist (check by email)
        if (!userRepository.findByEmail("juan@example.com").isPresent()) {
            userRepository.save(new User("Juan Perez", "juan@example.com", "123456", "admin"));
        }
        
        if (!userRepository.findByEmail("maria@example.com").isPresent()) {
            maria = userRepository.save(new User("Maria Lopez", "maria@example.com", "password", "user"));
        } else {
            maria = userRepository.findByEmail("maria@example.com").orElse(null);
        }
        
        if (!userRepository.findByEmail("miryam@example.com").isPresent()) {
            userRepository.save(new User("Miryam Merchan", "miryam@example.com", "charlie", "maintenance"));
        }
        
        if (!userRepository.findByEmail("carmen@example.com").isPresent()) {
            userRepository.save(new User("Carmen", "carmen@example.com", "uuuu", "user"));
        }

        // Create penalty for Maria only if it doesn't exist
        if (maria != null && penaltyRepository.count() == 0) {
            System.out.println("Creating penalty for Maria Lopez with userId: " + maria.getUserId());
            penaltyRepository.save(new Penalty(maria.getUserId(), "Did not show up without canceling", LocalDateTime.now().minusDays(1)));
        }

        
    }
}