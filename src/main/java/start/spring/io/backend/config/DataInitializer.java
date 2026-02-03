package start.spring.io.backend.config;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import start.spring.io.backend.model.Facility;
import start.spring.io.backend.model.MaintenanceRequest;
import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.model.Reservation;
import start.spring.io.backend.model.User;
import start.spring.io.backend.repository.FacilityRepository;
import start.spring.io.backend.repository.MaintenanceRequestRepository;
import start.spring.io.backend.repository.PenaltyRepository;
import start.spring.io.backend.repository.ReservationRepository;
import start.spring.io.backend.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FacilityRepository facilityRepository;
    private final UserRepository userRepository;
    private final PenaltyRepository penaltyRepository;
    private final MaintenanceRequestRepository maintenanceRepository;
    private final ReservationRepository reservationRepository; // Añadido

    public DataInitializer(
            FacilityRepository facilityRepository,
            UserRepository userRepository,
            PenaltyRepository penaltyRepository,
            MaintenanceRequestRepository maintenanceRepository,
            ReservationRepository reservationRepository
    ) {
        this.facilityRepository = facilityRepository;
        this.userRepository = userRepository;
        this.penaltyRepository = penaltyRepository;
        this.maintenanceRepository = maintenanceRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Facilities
        if (facilityRepository.count() == 0) {
            facilityRepository.save(new Facility("Tennis Court 1", "Tennis", "Available"));
            facilityRepository.save(new Facility("Badminton Court 1", "Badminton", "Unavailable"));
            facilityRepository.save(new Facility("Ping Pong 1", "Ping Pong", "Available"));
            facilityRepository.save(new Facility("Padel Court 1", "Padel", "Available"));
            facilityRepository.save(new Facility("Soccer Field Main", "Football", "Available"));
            facilityRepository.save(new Facility("Basketball Court", "Basketball", "Available"));
        }

        // 2. Users
        // Admin
        if (!userRepository.findByEmail("admin@test.com").isPresent()) {
            userRepository.save(new User("Reservation Manager", "admin@test.com", "$2a$12$fhX51HXW8X7YwQKQVLUep.D457PdylfzXn2TeGH.wMuZljIDMdnze", "admin"));
        }
        // User normal
        if (!userRepository.findByEmail("user@test.com").isPresent()) {
            userRepository.save(new User("Regular User", "user@test.com", "$2a$12$MNyGGaKA5F.QPaCENXm5pOT.qyaMhI.AqAwpgTCSSLjWLbN/xpXaS", "user"));
        }
        // Maintenance
        if (!userRepository.findByEmail("manteinance@test.com").isPresent()) {
            userRepository.save(new User("Manteinance Staff", "manteinance@test.com", "$2a$12$IdWu/vuRLHEkHfKLXOPjReiWUcFh7QHOGNAbNM0FPvlTdWGj945fi", "maintenance"));
        }
        // Marta (Tu usuario de pruebas)
        User marta;
        if (!userRepository.findByEmail("marta@test.com").isPresent()) {
            marta = userRepository.save(new User("Marta", "marta@test.com", "$2a$12$RySmtaQLiW6cxrdt/fsqn.PsTTedmS1ieJ7pGgVpjegeyP8Ddb.MS", "user"));
        } else {
            marta = userRepository.findByEmail("marta@test.com").get();
        }

        // 3. Reservas de Prueba (ACTUALIZADO PARA JPA)
        if (reservationRepository.count() == 0) {
            // Buscamos una pista y un usuario reales
            Facility padel = facilityRepository.findAll().stream()
                    .filter(f -> f.getType().equals("Padel"))
                    .findFirst().orElse(null);

            if (padel != null && marta != null) {
                Reservation r = new Reservation();
                r.setFacility(padel); // Asignamos OBJETO
                r.setUser(marta);     // Asignamos OBJETO
                r.setDate(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
                r.setStartTime(LocalTime.of(10, 0));
                r.setEndTime(LocalTime.of(11, 30));
                r.setParticipants(4);
                r.setPurpose("Training match");

                reservationRepository.save(r);
            }
        }

        // 4. Penalty
        if (marta != null && penaltyRepository.count() == 0) {
            penaltyRepository.save(new Penalty(marta, "Did not show up without canceling", LocalDateTime.now().minusDays(1)));
        }

        // 5. Maintenance Request
        if (marta != null && maintenanceRepository.count() == 0) {
            Facility badminton = facilityRepository.findAll().stream()
                    .filter(f -> f.getName().contains("Badminton"))
                    .findFirst()
                    .orElse(null);

            if (badminton != null) {
                MaintenanceRequest req = new MaintenanceRequest();
                req.setUser(marta);
                req.setFacility(badminton);
                req.setIssueType("Lighting Failure");
                req.setDescription("The main lights are flickering.");
                req.setSeverity("HIGH");
                req.setStatus("IN_PROGRESS");
                req.setReportDate(LocalDateTime.now().minusHours(4));

                maintenanceRepository.save(req);
            }
        }
    }
}