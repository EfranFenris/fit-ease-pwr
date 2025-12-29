package start.spring.io.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.repository.FacilityRepository;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;

    public DataInitializer(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository; 
    }

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (facilityRepository.count() == 0) {  // Solo si no hay usuarios
            facilityRepository.save(new Facility("Tennis-Court-1", "outdoors", "free"));
            facilityRepository.save(new Facility("Badminton-Court-1", "indoors", "Under-Maintenance"));
            facilityRepository.save(new Facility("Ping-Pong-1", "indoors","booked"));
            facilityRepository.save(new Facility("Padel-Court-1", "outdoors", "free"));
        }

        if (userRepository.count() == 0) {
            userRepository.save(new User("Juan Perez", "juan@example.com", "123456", "admin"));
            userRepository.save(new User("Maria Lopez", "maria@example.com", "password", "user"));
            userRepository.save(new User("Miryam Merchan", "miryam@example.com", "charlie", "manteinance"));
            userRepository.save(new User("Carmen", "carmen@example.com", "uuuu", "user"));
        }
    }
}
