package start.spring.io.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import start.spring.io.backend.model.Facility;

/**
 * This repository manages the "Facility" table (Tennis courts, Soccer fields, etc.).
 * By extending 'JpaRepository', we automatically get methods like:
 * - save() -> Create or Update a court
 * - findById() -> Get details of one court
 * - findAll() -> Get a list of all courts
 */
public interface FacilityRepository extends JpaRepository<Facility, Integer> {
}