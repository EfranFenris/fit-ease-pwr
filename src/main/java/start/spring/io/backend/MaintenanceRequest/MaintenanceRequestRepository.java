package start.spring.io.backend.MaintenanceRequest;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for the MaintenanceRequest entity.
 * Provides basic CRUD operations.
 */
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Integer> {
}
