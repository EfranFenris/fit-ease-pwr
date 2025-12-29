package start.spring.io.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.repository.FacilityRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FacilityRepository facilityRepository;

    public DataInitializer(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (facilityRepository.count() == 0) {  // Solo si no hay usuarios
            facilityRepository.save(new Facility("Tennis-Court-1", "outdoors", "free"));
            facilityRepository.save(new Facility("Badminton-Court-1", "indoors", "Under-Maintenance"));
            facilityRepository.save(new Facility("Ping-Pong-1", "indoors","booked"));
            facilityRepository.save(new Facility("Padel-Court-1", "outdoors", "free"));
        }
    }
}