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

@Controller
@RequestMapping("/calendar")
public class CalendarController {

    private final ReservationService reservationService;

    public CalendarController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // 1. Mostrar la Vista HTML
    @GetMapping
    public String showCalendar(Model model) {
        model.addAttribute("currentPage", "calendar");
        return "calendar";
    }

    // 2. API JSON para FullCalendar
    @GetMapping("/events")
    @ResponseBody
    public List<Map<String, Object>> getCalendarEvents() {
        List<Reservation> allReservations = reservationService.getAll();

        return allReservations.stream().map(r -> {
            String facilityName = (r.getFacility() != null) ? r.getFacility().getName() : "Unknown Facility";

            // Construimos fecha inicio y fin combinando Date + Time
            LocalDateTime startDateTime = LocalDateTime.of(r.getDate().toLocalDate(), r.getStartTime());
            LocalDateTime endDateTime = LocalDateTime.of(r.getDate().toLocalDate(), r.getEndTime());

            // SOLUCIÓN AL ERROR:
            // Usamos 'Map.<String, Object>of' para decirle a Java explícitamente
            // que queremos un Map de Objects, no solo de Strings.
            return Map.<String, Object>of(
                    "title", facilityName + " (Occupied)",
                    "start", startDateTime.toString(),
                    "end", endDateTime.toString(),
                    "color", "#ef4444",
                    "textColor", "#ffffff"
            );
        }).collect(Collectors.toList());
    }
}