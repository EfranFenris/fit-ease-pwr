package start.spring.io.backend.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.UserRepository;

/**
 * This service is a bridge between our Database and Spring Security.
 * This class translates our User -> Spring User.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method is called automatically when someone tries to log in.
     * 1. It looks for the user by email in our database.
     * 2. If found, it converts the data into a format Spring Security understands (Email, Password, Role).
     * 3. If not found, it throws an error.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Map your domain user to Spring Security user
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole()) // Spring adds "ROLE_" automatically (for example "admin" -> "ROLE_admin")
                .build();
    }
}