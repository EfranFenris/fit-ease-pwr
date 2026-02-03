package start.spring.io.backend.dto;

import java.time.LocalDateTime;
import start.spring.io.backend.model.Reservation;

/**
 * This record prepares the data for the "My Reservations" list.
 * Instead of asking the HTML page to calculate dates or decide if a penalty
 * applies, we do all that hard work in Java and send the final results here.
 */
public record ReservationCardView(
        Reservation reservation,    // The original booking data
        String facilityName,        // Easier to access than reservation.getFacility().getName()
        String facilityType,        // like: "Tennis", "Padel"
        String location,
        String imageUrl,            // Visual background for the card
        String statusLabel,         // "Upcoming" or "Past"
        String statusClass,         // CSS style
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        int participants,
        String purpose,
        boolean incursPenalty       // Tells the user "If you cancel now, you get a strike!"
) {
}