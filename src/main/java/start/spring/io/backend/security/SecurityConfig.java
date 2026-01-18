package start.spring.io.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import start.spring.io.backend.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            // 1. Configure authorization rules (from specific to general)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**", "/images/**").permitAll() // Allow public access to these paths
                .requestMatchers("/facilities/**").authenticated() // All authenticated users can access
                .requestMatchers("/reservations/**").authenticated() // All authenticated users can access
                .requestMatchers("/users/**").hasRole("admin") // Only admin can access users
                .requestMatchers("/maintenance-requests/dashboard").hasRole("admin") // Only admin can access dashboard
                .requestMatchers("/maintenance-requests/**").authenticated() // All authenticated users can create requests
                .requestMatchers("/admin/**").hasRole("admin") // Only admin can access
                .anyRequest().authenticated() // All other requests require authentication
            )

            // 2. Configure form login
            .formLogin(form -> form
                .loginPage("/login") // Custom login page at this URL
                .defaultSuccessUrl("/facilities", true) // Redirect there on successful login
                .permitAll()
            )
            
            // 3. Configure access denied handling
            .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendError(403, "Access Denied");
                })
            )
            
            // 3. Configure logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/") // Redirect to landing page on logout
                .permitAll());

        return http.build();
    }

}