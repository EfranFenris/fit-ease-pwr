package start.spring.io.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import start.spring.io.backend.model.Facility;

public interface FacilityRepository extends JpaRepository<Facility, Integer> {
}
