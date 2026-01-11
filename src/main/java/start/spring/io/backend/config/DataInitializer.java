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

    public DataInitializer(FacilityRepository facilityRepository, UserRepository userRepository) {
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.penaltyRepository = penaltyRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        User maria = null;

        if (facilityRepository.count() == 0) {  // Solo si no hay usuarios
            facilityRepository.save(new Facility("Tennis-Court-1", "outdoors", "free"));
            facilityRepository.save(new Facility("Badminton-Court-1", "indoors", "Under-Maintenance"));
            facilityRepository.save(new Facility("Ping-Pong-1", "indoors","booked"));
            facilityRepository.save(new Facility("Padel-Court-1", "outdoors", "free"));
        }

        if (userRepository.count() == 0) {
            userRepository.save(new User("Juan Perez", "juan@example.com", "123456", "admin"));
            maria = userRepository.save(new User("Maria Lopez", "maria@example.com", "password", "user"));
            userRepository.save(new User("Miryam Merchan", "miryam@example.com", "charlie", "manteinance"));
            userRepository.save(new User("Maria Lopez", "maria@example.com", "password", "user"));
            userRepository.save(new User("Miryam Merchan", "miryam@example.com", "charlie", "maintenance"));
            userRepository.save(new User("Carmen", "carmen@example.com", "uuuu", "user"));
        }

        if (penaltyRepository.count() == 0) {
            if (maria != null) {
                System.out.println("Creating penalty for Maria Lopez with userId: " + maria.getUserId());
                penaltyRepository.save(new Penalty(maria.getUserId(), "Did not show up without canceling", LocalDateTime.now().minusDays(1)));
            }
        }

        
    }
}