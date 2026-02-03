package start.spring.io.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder; // Importante
import org.springframework.stereotype.Service;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder; // Inyectamos esto

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() { return repository.findAll(); }
    public Optional<User> getUserById(Integer id) { return repository.findById(id); }
    public Optional<User> getUserByEmail(String email) { return repository.findByEmail(email); }

    public User createUser(User request) {
        request.setUserId(null);
        // Encriptar contraseña al crear
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        return repository.save(request);
    }

    public Optional<User> updateUser(Integer id, User userDetails) {
        return repository.findById(id).map(existingUser -> {
            existingUser.setName(userDetails.getName());
            existingUser.setEmail(userDetails.getEmail());
            existingUser.setRole(userDetails.getRole());

            // LÓGICA IMPORTANTE: Reset de contraseña
            // Solo cambiamos la contraseña si el campo NO está vacío.
            // Así, si el admin edita el nombre pero deja la pass vacía, no se rompe nada.
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            // Dentro de updateUser...
            if (userDetails.getRole() != null) { // <--- AÑADE ESTE IF
                existingUser.setRole(userDetails.getRole());
            }

            return repository.save(existingUser);
        });
    }

    // --- NUEVO MÉTODO: Obtener usuarios por rol ---
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