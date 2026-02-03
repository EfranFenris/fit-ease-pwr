package start.spring.io.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import start.spring.io.backend.service.CustomUserDetailsService;

/**
 * This is the Security Configuration.
 * It defines the rules of the endpoints:
 * - Which doors (URLs) are open to all users
 * - Which doors require a key (Login)
 * - How do we check the keys (Passwords)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationHandler authenticationHandler;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          AuthenticationHandler authenticationHandler,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.authenticationHandler = authenticationHandler;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * This method sets up the "Authentication Provider".
     * This is the logic that connects our Login Form to our Database.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        // We use the injected passwordEncoder (defined in ApplicationConfig)
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * This is the Firewall.
     * We use a chain of filters to intercept every request coming from the browser.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ZONES: Anyone can see the landing page, login, signup, and CSS/Images
                        .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**", "/images/**").permitAll()

                        // RESTRICTED ZONES:
                        // Facilities can be seen by Users and Admins
                        .requestMatchers("/facilities/**").hasAnyRole("user", "admin")

                        // Only logged-in people can see the calendar
                        .requestMatchers("/calendar/**").authenticated()

                        // Only Admins can see the Manager Dashboard
                        .requestMatchers("/reservations/manager/**").hasRole("admin")
                        .requestMatchers("/reservations/**").hasAnyRole("user", "admin")

                        // Maintenance forms
                        .requestMatchers("/maintenance-requests/maintenance-request-form/**", "/maintenance-requests/add").hasAnyRole("user", "admin")
                        .requestMatchers("/maintenance-requests/**").hasAnyRole("maintenance")

                        // Admin only areas
                        .requestMatchers("/users/**").hasRole("admin")
                        .requestMatchers("/admin/**").hasRole("admin")

                        // Everyone can see their own profile
                        .requestMatchers("/profile/**").authenticated()

                        // If we forgot a rule, we block it by default
                        .anyRequest().authenticated()
                )
                // Configure the Login Page
                .formLogin(form -> form
                        .loginPage("/login") // We have our own custom HTML page
                        .successHandler(authenticationHandler) // Where to go after success
                        .permitAll()
                )
                // Handle "Access Denied" (for example a regular user trying to enter Admin area)
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/access-denied");
                        })
                )
                // Configure Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/") // Go back to Landing page
                        .permitAll());

        return http.build();
    }
}