package start.spring.io.backend.service;

import org.springframework.stereotype.Service;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;

    /** Injects the repository dependency. */
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /** Get all users. */
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    /** Get a user by id. */
    public Optional<User> getUserById(Integer id) {
        return repository.findById(id);
    }

    /** Create a new user. */
    public User createUser(User request) {
        request.setUserId(null);
        return repository.save(request);
    }

    /** Update an existing user. */
    public Optional<User> updateUser(Integer id, User userDetails) {
        return repository.findById(id).map(request -> {
            request.setName(userDetails.getName());
            request.setEmail(userDetails.getEmail());
            request.setPassword(userDetails.getPassword());
            request.setRole(userDetails.getRole());
            return repository.save(request);
        });
    }

    /** Delete a user by id. */
    public boolean deleteUser(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
