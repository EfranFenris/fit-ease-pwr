package start.spring.io.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import start.spring.io.backend.model.MaintenanceRequest;

public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Integer> {

    @Query("""
    SELECT r FROM MaintenanceRequest r
    WHERE (:status IS NULL OR :status = '' OR r.status = :status)
    ORDER BY 
      CASE r.status WHEN 'PENDING' THEN 0 WHEN 'IN_PROGRESS' THEN 1 WHEN 'RESOLVED' THEN 2 ELSE 3 END,
      CASE r.severity WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 WHEN 'LOW' THEN 2 ELSE 3 END,
      r.reportDate DESC
    """)
    List<MaintenanceRequest> findFiltered(String status);

    List<MaintenanceRequest> findByStatus(String status);

    // CORRECCIÓN AQUÍ:
    // Usamos 'Facility_FacilityId' (con guion bajo) para decirle a Spring explícitamente:
    // "Entra en el objeto Facility y busca el campo facilityId"
    boolean existsByFacility_FacilityIdAndStatusNot(Integer facilityId, String status);

}