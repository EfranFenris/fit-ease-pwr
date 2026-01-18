package start.spring.io.backend.repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import start.spring.io.backend.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findByUserId(Integer userId);

    @Query("""
            select r
            from Reservation r
            where r.facilityId = :facilityId
              and r.date >= :dayStart
              and r.date < :dayEnd
              and r.startTime < :endTime
              and r.endTime > :startTime
              and (:excludeId is null or r.reservationId <> :excludeId)
            """)
    List<Reservation> findConflicts(
            @Param("facilityId") Integer facilityId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Integer excludeId);
}
