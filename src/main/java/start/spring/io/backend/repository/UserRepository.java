package start.spring.io.backend.repository;

import java.util.List; // Importar List
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import start.spring.io.backend.model.User;

/**
 * This repository manages User Accounts.
 * It is used primarily during Login, Signup, and Admin management.
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Finds a user by their email address.
     * Used for the Login process (Authentication).
     * It returns an 'Optional' because the user might not exist.
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a list of users based on their role.
     * Example: findByRole("user") -> Gets all normal players.
     * Example: findByRole("maintenance") -> Gets all staff members.
     */
    List<User> findByRole(String role);
}