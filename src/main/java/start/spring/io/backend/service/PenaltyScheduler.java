package start.spring.io.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import start.spring.io.backend.repository.PenaltyRepository;

import java.time.LocalDateTime;

/**
 * This is an automated service.
 * It uses the @Scheduled annotation to run tasks automatically in the background.
 */
@Service
public class PenaltyScheduler {

    private final PenaltyRepository penaltyRepository;

    public PenaltyScheduler(PenaltyRepository penaltyRepository) {
        this.penaltyRepository = penaltyRepository;
    }

    /**
     * This method runs automatically every day at 3:00 AM.
     * It deletes penalties that are older than 3 months to be fair to users.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void removeExpiredPenalties() {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        System.out.println("Running maintenance: Deleting penalties older than " + threeMonthsAgo);

        penaltyRepository.deleteByDatehourBefore(threeMonthsAgo);
    }
}