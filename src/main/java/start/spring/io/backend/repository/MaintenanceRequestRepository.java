package start.spring.io.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import start.spring.io.backend.model.MaintenanceRequest;

/**
 * This repository handles the list of broken equipment reports.
 * It includes a special custom query to help the Maintenance Staff see
 * the most urgent problems first.
 */
public interface MaintenanceRequestRepository extends JpaRepository<MaintenanceRequest, Integer> {

    /**
     * This is a custom search query (written in JPQL).
     * It does two things:
     * 1. Filters by status (like show only "PENDING" requests).
     * 2. Sorts the results intelligently:
     * - Status: Pending items appear before Resolved ones.
     * - Severity: HIGH priority items appear before LOW priority ones.
     * - Date: Newest reports appear first.
     */
    @Query("""
    SELECT r FROM MaintenanceRequest r
    WHERE (:status IS NULL OR :status = '' OR r.status = :status)
    ORDER BY 
      CASE r.status WHEN 'PENDING' THEN 0 WHEN 'IN_PROGRESS' THEN 1 WHEN 'RESOLVED' THEN 2 ELSE 3 END,
      CASE r.severity WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 WHEN 'LOW' THEN 2 ELSE 3 END,
      r.reportDate DESC
    """)
    List<MaintenanceRequest> findFiltered(String status);

    /**
     * Simple finder: Get all requests that match a specific status string.
     */
    List<MaintenanceRequest> findByStatus(String status);

    /**
     * Checks if a facility is currently broken.
     * "Is there any report for this facility that is NOT yet resolved?"
     */
    boolean existsByFacility_FacilityIdAndStatusNot(Integer facilityId, String status);

}