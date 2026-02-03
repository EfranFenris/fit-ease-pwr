package start.spring.io.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder; // Importante
import org.springframework.stereotype.Service;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * This service handles User Account Management.
 * Its makes sure passwords are never saved as plain text but are always encrypted.
 */
@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder; // We inject the encryption tool

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() { return repository.findAll(); }
    public Optional<User> getUserById(Integer id) { return repository.findById(id); }
    public Optional<User> getUserByEmail(String email) { return repository.findByEmail(email); }

    /**
     * Creates a new user (Signup).
     * We immediately encrypt the password using 'passwordEncoder.encode()'
     * before sending it to the database.
     */
    public User createUser(User request) {
        request.setUserId(null);
        // Encrypt password before saving
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        return repository.save(request);
    }

    /**
     * Updates user details (Profile Edit).
     * If the user leaves the password field empty, we keep their old password.
     * If the user types a new password, we encrypt it and update it.
     */
    public Optional<User> updateUser(Integer id, User userDetails) {
        return repository.findById(id).map(existingUser -> {
            existingUser.setName(userDetails.getName());
            existingUser.setEmail(userDetails.getEmail());

            // Only change password if the user actually typed something new
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            // Only update the role if a new one is provided
            if (userDetails.getRole() != null) {
                existingUser.setRole(userDetails.getRole());
            }

            return repository.save(existingUser);
        });
    }

    /**
     * Helper to find all users by their role (example: all Admins)
     */
    public List<User> getUsersByRole(String role) {
        return repository.findByRole(role);
    }

    public boolean deleteUser(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}