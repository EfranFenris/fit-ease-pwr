package start.spring.io.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import start.spring.io.backend.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
}
