package start.spring.io.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import start.spring.io.backend.repository.PenaltyRepository;

import java.time.LocalDateTime;

@Service
public class PenaltyScheduler {

    private final PenaltyRepository penaltyRepository;

    public PenaltyScheduler(PenaltyRepository penaltyRepository) {
        this.penaltyRepository = penaltyRepository;
    }

    // Se ejecuta todos los días a las 3:00 AM
    // Cron format: seg min hora dia mes dia_semana
    @Scheduled(cron = "0 0 3 * * *")
    public void removeExpiredPenalties() {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        System.out.println("Running maintenance: Deleting penalties older than " + threeMonthsAgo);

        penaltyRepository.deleteByDatehourBefore(threeMonthsAgo);
    }
}