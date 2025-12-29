package start.spring.io.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import start.spring.io.backend.model.MaintenanceRequest;

/**
 * JPA repository for the MaintenanceRequest entity.
 * Provides basic CRUD operations.
 */
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Integer> {
}

