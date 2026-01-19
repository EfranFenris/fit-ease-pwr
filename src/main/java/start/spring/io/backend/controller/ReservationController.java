package start.spring.io.backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import start.spring.io.backend.dto.ReservationCardView;
import start.spring.io.backend.model.Facility;
import start.spring.io.backend.model.Reservation;
import start.spring.io.backend.model.User;
import start.spring.io.backend.service.FacilityService;
import start.spring.io.backend.service.ReservationService;
import start.spring.io.backend.service.UserService;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;
    private final UserService userService;
    private final FacilityService facilityService;

    public ReservationController(ReservationService service, UserService userService, FacilityService facilityService) {
        this.service = service;
        this.userService = userService;
        this.facilityService = facilityService;
    }

    @GetMapping
    public String list(Model model,
            Authentication authentication,
            @RequestParam(value = "filter", defaultValue = "upcoming") String filter) {
        Optional<User> user = getAuthenticatedUser(authentication);
        Integer userId = user.map(User::getUserId).orElse(null);
        List<Reservation> reservations = userId == null
                ? Collections.emptyList()
                : service.getByUserId(userId);
        List<ReservationCardView> cards = reservations.stream()
                .map(this::toCardView)
                .filter(card -> matchesFilter(card, filter))
                .toList();

        String userName = user.map(User::getName).orElse("Guest");

        model.addAttribute("reservations", reservations);
        model.addAttribute("reservationCards", cards);
        model.addAttribute("filter", filter);
        model.addAttribute("userName", userName);
        model.addAttribute("currentPage", "reservations");
        model.addAttribute("newReservation", new Reservation());
        return "reservation-list";
    }

    @GetMapping("/new/{facilityId}")
    public String newReservation(@PathVariable Integer facilityId, Model model) {
        String facilityName = facilityService.getFacilityById(facilityId)
                .map(Facility::getName)
                .orElse("Selected Facility");
        model.addAttribute("facilityId", facilityId);
        model.addAttribute("facilityName", facilityName);
        return "reservation-booking";
    }

    @PostMapping("/book")
    public String bookReservation(@RequestParam("facilityId") Integer facilityId,
            @RequestParam("bookingDate") String bookingDate,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime,
            @RequestParam("participants") Integer participants,
            @RequestParam(value = "purpose", required = false) String purpose,
            Authentication authentication) {
        Reservation reservation = new Reservation();
        reservation.setFacilityId(facilityId);
        reservation.setParticipants(participants);
        reservation.setPurpose(purpose);

        LocalDate date = LocalDate.parse(bookingDate);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        reservation.setDate(LocalDateTime.of(date, start));
        reservation.setStartTime(start);
        reservation.setEndTime(end);

        Integer userId = 1;
        if (authentication != null && authentication.isAuthenticated()) {
            userId = userService.getUserByEmail(authentication.getName())
                    .map(User::getUserId)
                    .orElse(1);
        }
        reservation.setUserId(userId);

        service.create(reservation);
        return "redirect:/facilities";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute("newReservation") Reservation reservation) {
        service.create(reservation);
        return "redirect:/reservations";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Reservation r = service.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + id));
        model.addAttribute("reservation", r);
        return "reservation-edit";
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, @ModelAttribute("reservation") Reservation reservation) {
        service.update(id, reservation);
        return "redirect:/reservations";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        service.delete(id);
        return "redirect:/reservations";
    }

    private Optional<User> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return userService.getUserByEmail(authentication.getName());
    }

    private boolean matchesFilter(ReservationCardView card, String filter) {
        if ("past".equalsIgnoreCase(filter)) {
            return "Past".equalsIgnoreCase(card.statusLabel());
        }
        if ("all".equalsIgnoreCase(filter)) {
            return true;
        }
        return "Upcoming".equalsIgnoreCase(card.statusLabel());
    }

    private ReservationCardView toCardView(Reservation reservation) {
        Facility facility = facilityService.getFacilityById(reservation.getFacilityId()).orElse(null);
        String facilityName = facility != null ? facility.getName() : "Facility #" + reservation.getFacilityId();
        String type = facility != null && facility.getType() != null ? facility.getType().toLowerCase() : "";

        String imageUrl = switch (type) {
            case "tennis", "padel" ->
                "https://images.unsplash.com/photo-1508609349937-5ec4ae374ebf?auto=format&fit=crop&w=1000&q=80";
            case "basketball" ->
                "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1000&q=80";
            case "soccer", "football" ->
                "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?auto=format&fit=crop&w=1000&q=80";
            case "badminton" ->
                "https://images.unsplash.com/photo-1517649763962-0c623066013b?auto=format&fit=crop&w=1000&q=80";
            default ->
                "https://images.unsplash.com/photo-1471295253337-3ceaaedca402?auto=format&fit=crop&w=1000&q=80";
        };

        String location = switch (type) {
            case "tennis", "padel" -> "Racquet Zone";
            case "basketball" -> "Central Pavilion";
            case "soccer", "football" -> "North Sports Complex";
            case "badminton" -> "Indoor Hall";
            default -> "Main Sports Hub";
        };

        LocalDateTime startDateTime = reservation.getDate();
        LocalDateTime endDateTime = reservation.getDate().with(reservation.getEndTime());
        boolean isPast = startDateTime.isBefore(LocalDateTime.now());
        String statusLabel = isPast ? "Past" : "Upcoming";
        String statusClass = isPast ? "status-past" : "";
        String purpose = reservation.getPurpose() == null || reservation.getPurpose().isBlank()
                ? "General Booking"
                : reservation.getPurpose();

        return new ReservationCardView(
                reservation,
                facilityName,
                facility != null ? facility.getType() : "",
                location,
                imageUrl,
                statusLabel,
                statusClass,
                startDateTime,
                endDateTime,
                reservation.getParticipants() == null ? 0 : reservation.getParticipants(),
                purpose);
    }
}
