package start.spring.io.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; <--- YA NO SE NECESITA AQUÍ
import org.springframework.security.crypto.password.PasswordEncoder; // Solo la interfaz
import org.springframework.security.web.SecurityFilterChain;

import start.spring.io.backend.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationHandler authenticationHandler;
    private final PasswordEncoder passwordEncoder; // <--- 1. NUEVA VARIABLE

    // 2. AÑADIMOS passwordEncoder AL CONSTRUCTOR
    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          AuthenticationHandler authenticationHandler,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.authenticationHandler = authenticationHandler;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        // 3. USAMOS LA VARIABLE INYECTADA, NO EL MÉTODO
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // 4. ELIMINAMOS EL MÉTODO @Bean passwordEncoder() DE AQUÍ
    // (Ya lo movimos a ApplicationConfig.java)

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**", "/images/**").permitAll()

                        .requestMatchers("/facilities/**").hasAnyRole("user", "admin")
                        .requestMatchers("/calendar/**").authenticated()
                        .requestMatchers("/reservations/manager/**").hasRole("admin")
                        .requestMatchers("/reservations/**").hasAnyRole("user", "admin")

                        .requestMatchers("/maintenance-requests/maintenance-request-form/**", "/maintenance-requests/add").hasAnyRole("user", "admin")
                        .requestMatchers("/maintenance-requests/**").hasAnyRole("maintenance")

                        .requestMatchers("/users/**").hasRole("admin")
                        .requestMatchers("/admin/**").hasRole("admin")
                        .requestMatchers("/profile/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(authenticationHandler)
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/access-denied");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll());

        return http.build();
    }
}