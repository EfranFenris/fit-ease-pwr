package start.spring.io.backend.dto;

import java.time.LocalDateTime;

import start.spring.io.backend.model.Reservation;

public record ReservationCardView(
        Reservation reservation,
        String facilityName,
        String facilityType,
        String location,
        String imageUrl,
        String statusLabel,
        String statusClass,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        int participants,
        String purpose) {
}
