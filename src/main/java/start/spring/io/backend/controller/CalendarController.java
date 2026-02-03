package start.spring.io.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import start.spring.io.backend.model.Reservation;
import start.spring.io.backend.service.ReservationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; // <--- ESTE IMPORT ES CRUCIAL

/**
 * This controller manages the Calendar page.
 * It has two main jobs: showing the HTML page and providing the data (JSON)
 * so the calendar knows what events to display.
 */
@Controller
@RequestMapping("/calendar")
public class CalendarController {

    private final ReservationService reservationService;

    public CalendarController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    /**
     * Show the HTML View.
     * When the user visits "/calendar", this method runs.
     * It sets up the page and returns the "calendar.html" template.
     */
    @GetMapping
    public String showCalendar(Model model) {
        // We add an attribute so the navbar knows we are on the 'calendar' page
        model.addAttribute("currentPage", "calendar");
        return "calendar";
    }

    /**
     * API JSON for FullCalendar.
     * This method does NOT return an HTML page. With @ResponseBody,
     * it returns a list of data in JSON format. The JavaScript calendar in the browser
     * calls this URL to download the events.
     */
    @GetMapping("/events")
    @ResponseBody
    public List<Map<String, Object>> getCalendarEvents() {
        // First, we fetch all reservations from our database
        List<Reservation> allReservations = reservationService.getAll();

        // We use a stream to transform each Reservation object into the specific format
        // that the FullCalendar library requires (title, start, end, color).
        return allReservations.stream().map(r -> {
            // If the facility is null for some reason, we give it a default name
            String facilityName = (r.getFacility() != null) ? r.getFacility().getName() : "Unknown Facility";

            // We combine the Date (YYYY-MM-DD) and Time (HH:MM) to create a full timestamp
            LocalDateTime startDateTime = LocalDateTime.of(r.getDate().toLocalDate(), r.getStartTime());
            LocalDateTime endDateTime = LocalDateTime.of(r.getDate().toLocalDate(), r.getEndTime());

            // We create a Map with the specific keys the frontend expects.
            // Map.of creates a key-value pair object.
            return Map.<String, Object>of(
                    "title", facilityName + " (Occupied)",
                    "start", startDateTime.toString(),
                    "end", endDateTime.toString(),
                    "color", "#ef4444", // Red color for the event bar
                    "textColor", "#ffffff" // White text
            );
        }).collect(Collectors.toList());
    }
}