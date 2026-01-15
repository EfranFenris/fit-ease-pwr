package start.spring.io.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import start.spring.io.backend.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
}
