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
                    // 1. Rutas públicas (Static resources, login, etc.)
                    .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**", "/images/**").permitAll()

                    // 2. Rutas básicas para usuarios autenticados
                    .requestMatchers("/facilities/**").authenticated()
                    .requestMatchers("/reservations/**").authenticated()

                    // 3. LOGICA DE MANTENIMIENTO (El orden aquí es CRÍTICO)

                    // A) PRIMERO: Permitir el formulario de reporte a CUALQUIER usuario autenticado (Estudiantes/Profesores)
                    // Cubrimos tanto el GET (ver formulario) como el POST (enviar formulario)
                    .requestMatchers(
                            "/maintenance-requests/maintenance-request-form/**",
                            "/maintenance-requests/maintenance-request-form"
                    ).authenticated()

                    // B) SEGUNDO: Bloquear TODO lo demás de mantenimiento (Lista, Editar, Cambiar estado)
                    // Solo pueden entrar Maintenance Staff o Admin.
                    // Esto protege la url "/maintenance-requests" (tu lista/dashboard)
                    .requestMatchers("/maintenance-requests/**").hasAnyRole("maintenance", "admin")

                    // 4. Rutas de administración
                    .requestMatchers("/users/**").hasRole("admin")
                    .requestMatchers("/admin/**").hasRole("admin")

                    // 5. El resto requiere autenticación
                    .anyRequest().authenticated()
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