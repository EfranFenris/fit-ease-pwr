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
import start.spring.io.backend.model.Penalty;
import start.spring.io.backend.model.Reservation;
import start.spring.io.backend.model.User;
import start.spring.io.backend.service.EmailService;
import start.spring.io.backend.service.FacilityService;
import start.spring.io.backend.service.PenaltyService;
import start.spring.io.backend.service.ReservationService;
import start.spring.io.backend.service.UserService;

/**
 * This is the main controller for the application.
 * It handles the entire lifecycle of a Reservation:
 * 1. Listing bookings for a user.
 * 2. Creating new booking (with validation rules).
 * 3. Editing or Canceling bookings.
 * 4. The Manager Dashboard (Admin view).
 */
@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;
    private final UserService userService;
    private final FacilityService facilityService;
    private final PenaltyService penaltyService;
    private final EmailService emailService;

    public ReservationController(ReservationService service,
                                 UserService userService,
                                 FacilityService facilityService,
                                 PenaltyService penaltyService,
                                 EmailService emailService) {
        this.service = service;
        this.userService = userService;
        this.facilityService = facilityService;
        this.penaltyService = penaltyService;
        this.emailService = emailService;
    }

    /**
     * LIST RESERVATIONS ("My Bookings")
     * Displays the list of reservations for the currently logged-in user.
     * It allows filtering between "Upcoming" and "Past" events.
     */
    @GetMapping
    public String list(Model model, Authentication authentication, @RequestParam(value = "filter", defaultValue = "upcoming") String filter) {
        // Find who is logged in
        Optional<User> user = getAuthenticatedUser(authentication);
        Integer userId = user.map(User::getUserId).orElse(null);

        // Get their reservations from the database
        List<Reservation> reservations = userId == null ? Collections.emptyList() : service.getByUserId(userId);

        // Convert the raw data into "Cards" for the HTML view and apply the filter (Past vs Upcoming)
        List<ReservationCardView> cards = reservations.stream()
                .map(this::toCardView)
                .filter(card -> matchesFilter(card, filter))
                .toList();

        model.addAttribute("reservations", reservations);
        model.addAttribute("reservationCards", cards);
        model.addAttribute("filter", filter);
        model.addAttribute("userName", user.map(User::getName).orElse("Guest"));
        model.addAttribute("currentPage", "reservations");
        return "reservation-list";
    }

    /**
     * NEW RESERVATION FORM
     * Shows the booking form for a specific facility.
     */
    @GetMapping("/new/{facilityId}")
    public String newReservation(@PathVariable Integer facilityId, Model model) {
        Facility facility = facilityService.getFacilityById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));

        // Pass the necessary info to the form
        model.addAttribute("facilityId", facilityId);
        model.addAttribute("facilityName", facility.getName());
        model.addAttribute("maxParticipants", facilityService.getCapacityForType(facility.getType()));

        return "reservation-booking";
    }

    /**
     * REBOOK
     * A convenience feature. It copies the details of an old reservation
     * so the user can easily book the same thing again.
     */
    @GetMapping("/rebook/{reservationId}")
    public String rebookReservation(@PathVariable Integer reservationId, Model model) {
        Reservation oldReservation = service.getById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        Facility facility = oldReservation.getFacility(); // JPA Directo

        // Calculate how long the previous game lasted
        long durationMinutes = java.time.Duration.between(oldReservation.getStartTime(), oldReservation.getEndTime()).toMinutes();

        // Fill the form with the old values
        model.addAttribute("facilityId", facility.getFacilityId());
        model.addAttribute("facilityName", facility.getName());
        model.addAttribute("defaultParticipants", oldReservation.getParticipants());
        model.addAttribute("defaultPurpose", oldReservation.getPurpose());
        model.addAttribute("defaultDuration", durationMinutes);
        model.addAttribute("defaultStartTime", oldReservation.getStartTime());
        model.addAttribute("maxParticipants", facilityService.getCapacityForType(facility.getType()));

        return "reservation-booking";
    }

    /**
     * PROCESS BOOKING
     * This is the logic center. It validates the user's request:
     * - Is the facility open?
     * - Is the time valid?
     * - Is the slot already taken?
     * If all is good, it saves the booking and sends an email.
     */
    @PostMapping("/book")
    public String bookReservation(@RequestParam("facilityId") Integer facilityId,
                                  @RequestParam("bookingDate") String bookingDate,
                                  @RequestParam("startTime") String startTime,
                                  @RequestParam("endTime") String endTime,
                                  @RequestParam("participants") Integer participants,
                                  @RequestParam(value = "purpose", required = false) String purpose,
                                  Authentication authentication,
                                  Model model) {

        LocalDate date = LocalDate.parse(bookingDate);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        LocalDateTime bookingStart = LocalDateTime.of(date, start);

        // RULE
        // Users cannot book a slot that starts in 2 minutes. They need at least 10 min prior.
        if (bookingStart.isBefore(LocalDateTime.now().plusMinutes(10))) {
            return bookingError(model, facilityId, "You must book at least 10 minutes in advance.");
        }

        // End time must be after Start time
        if (!end.isAfter(start)) return bookingError(model, facilityId, "End time must be after the start time.");

        // Check if facility is physically available
        Facility facility = facilityService.getFacilityById(facilityId).orElseThrow();
        if (!"Available".equalsIgnoreCase(facility.getStatus()) && !"Free".equalsIgnoreCase(facility.getStatus())) {
            return bookingError(model, facilityId, "Facility unavailable.");
        }

        // Check capacity
        if (participants > facilityService.getCapacityForType(facility.getType())) return bookingError(model, facilityId, "Too many participants.");

        // Create the object
        Reservation reservation = new Reservation();
        reservation.setParticipants(participants);
        reservation.setPurpose(purpose);
        reservation.setDate(LocalDateTime.of(date, start));
        reservation.setStartTime(start);
        reservation.setEndTime(end);

        Integer userId = 1; // Default fallback user (if something goes wrong with auth)
        String userEmail = "";
        String userName = "User";

        // Link to the real logged-in user
        if (authentication != null && authentication.isAuthenticated()) {
            User currentUser = userService.getUserByEmail(authentication.getName()).orElse(null);
            if(currentUser != null) {
                userId = currentUser.getUserId();
                userEmail = currentUser.getEmail();
                userName = currentUser.getName();
            }
        }

        // Is the facility already booked by someone else?
        if (service.hasOverlap(facilityId, date, start, end)) return bookingError(model, facilityId, "Time slot booked.");
        // Does this specific user already have a booking at this time? (Double booking)
        if (service.hasUserOverlap(userId, date, start, end)) return bookingError(model, facilityId, "You have another booking.");

        // Save to DB
        service.create(reservation, userId, facilityId);

        // Send confirmation email
        if (!userEmail.isEmpty()) {
            String subject = "Booking Confirmed: " + facility.getName();
            String body = "Hello " + userName + ",\n\nYour booking for " + facility.getName() + " on " + date + " is confirmed.\n\nFitEasePWR Team";
            emailService.sendEmail(userEmail, subject, body);
        }

        return "redirect:/facilities";
    }

    /**
     * EDIT FORM
     * Shows the form to modify an existing reservation.
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Reservation r = service.getById(id).orElseThrow();
        Facility facility = r.getFacility();
        long durationMinutes = java.time.Duration.between(r.getStartTime(), r.getEndTime()).toMinutes();

        model.addAttribute("facilityId", facility.getFacilityId());
        model.addAttribute("facilityName", facility.getName());
        model.addAttribute("maxParticipants", facilityService.getCapacityForType(facility.getType()));
        model.addAttribute("defaultParticipants", r.getParticipants());
        model.addAttribute("defaultPurpose", r.getPurpose());
        model.addAttribute("defaultDuration", durationMinutes);
        model.addAttribute("defaultStartTime", r.getStartTime());
        model.addAttribute("defaultDate", r.getDate().toLocalDate());
        model.addAttribute("isEditMode", true);
        model.addAttribute("reservationId", id);
        return "reservation-booking";
    }

    /**
     * Process the update of a reservation.
     */
    @PostMapping("/edit/{id}")
    public String updateReservation(@PathVariable Integer id,
                                    @RequestParam("bookingDate") String bookingDate,
                                    @RequestParam("startTime") String startTime,
                                    @RequestParam("endTime") String endTime,
                                    @RequestParam("participants") Integer participants,
                                    @RequestParam(value = "purpose", required = false) String purpose) {
        LocalDate date = LocalDate.parse(bookingDate);
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);

        if (!end.isAfter(start)) return "redirect:/reservations";

        Reservation reservation = service.getById(id).orElseThrow();
        reservation.setDate(LocalDateTime.of(date, start));
        reservation.setStartTime(start);
        reservation.setEndTime(end);
        reservation.setParticipants(participants);
        reservation.setPurpose(purpose);
        service.update(id, reservation);
        return "redirect:/reservations";
    }

    /**
     * CANCEL RESERVATION
     * If the user cancels within 24 hours of the game, we apply a penalty.
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        Optional<Reservation> reservationOpt = service.getById(id);
        if (reservationOpt.isPresent()) {
            Reservation r = reservationOpt.get();
            LocalDateTime now = LocalDateTime.now();

            // If the game is in the future AND it is less than 24 hours away
            if (r.getDate().isAfter(now) && r.getDate().isBefore(now.plusHours(24))) {
                User user = r.getUser();
                if (user != null) {
                    // Create a penalty record
                    Penalty penalty = new Penalty();
                    penalty.setUser(user);
                    penalty.setDescription("Late cancellation for reservation ID " + id);
                    penalty.setDatehour(now);
                    penaltyService.createPenalty(penalty);

                    // Notify user
                    emailService.sendEmail(user.getEmail(), "Penalty Applied", "You have been penalized for late cancellation.");
                }
            }
        }
        service.delete(id);
        return "redirect:/reservations";
    }

    /**
     * MANAGER DASHBOARD
     * The special view for Admins to see today's agenda and manage penalties.
     */
    @GetMapping("/manager")
    public String managerDashboard(Model model) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        // Get only today's reservations
        List<ReservationCardView> cards = service.getReservationsByDateRange(start, end).stream().map(this::toCardView).toList();

        model.addAttribute("todaysReservations", cards);
        model.addAttribute("penalties", penaltyService.getAllPenalties());

        // Prepare data for the modals (Add Penalty, User List)
        model.addAttribute("newPenalty", new Penalty());
        model.addAttribute("users", userService.getUsersByRole("user"));
        model.addAttribute("currentPage", "manager");
        return "admin-reservations";
    }

    /**
     * MARK NO-SHOW
     * Admin clicks this if a user didn't turn up for their match.
     */
    @PostMapping("/admin/no-show/{id}")
    public String markNoShow(@PathVariable Integer id) {
        service.getById(id).ifPresent(res -> {

            // We give a 10-minute period to show up.
            // If the reservation started at 10:00, the admin cannot mark "No Show" until 10:11.
            LocalDateTime cutOffTime = res.getDate().plusMinutes(10);

            if (LocalDateTime.now().isBefore(cutOffTime)) {
                return; // Too early, do nothing.
            }

            User user = res.getUser();
            if (user != null) {
                String description = "No-Show: " + res.getDate();

                // Avoid punishing the user twice for the same reservation.
                if (!penaltyService.existsPenalty(user, description)) {
                    Penalty penalty = new Penalty();
                    penalty.setUser(user);
                    penalty.setDescription(description);
                    penalty.setDatehour(LocalDateTime.now());
                    penaltyService.createPenalty(penalty);

                    emailService.sendEmail(user.getEmail(), "Penalty Applied", "You missed your reservation.");
                }
            }
        });
        return "redirect:/reservations/manager";
    }

    // --- HELPER METHODS ---

    private Optional<User> getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return Optional.empty();
        return userService.getUserByEmail(authentication.getName());
    }

    private boolean matchesFilter(ReservationCardView card, String filter) {
        if ("past".equalsIgnoreCase(filter)) return "Past".equalsIgnoreCase(card.statusLabel());
        return "Upcoming".equalsIgnoreCase(card.statusLabel());
    }

    private ReservationCardView toCardView(Reservation reservation) {
        Facility facility = reservation.getFacility(); // JPA Directo
        String facilityName = facility != null ? facility.getName() : "Unknown";
        String type = facility != null ? facility.getType().toLowerCase() : "";

        String imageUrl = "https://images.unsplash.com/photo-1471295253337-3ceaaedca402?auto=format&fit=crop&w=1000&q=80";
        if(type.contains("tennis")) imageUrl = "https://images.unsplash.com/photo-1508609349937-5ec4ae374ebf?auto=format&fit=crop&w=1000&q=80";

        LocalDateTime start = reservation.getDate();
        boolean isPast = start.isBefore(LocalDateTime.now());
        String statusLabel = isPast ? "Past" : "Upcoming";
        String statusClass = isPast ? "status-past" : "";
        boolean incursPenalty = !isPast && start.isBefore(LocalDateTime.now().plusHours(24));

        return new ReservationCardView(reservation, facilityName, type, "Sports Hub", imageUrl, statusLabel, statusClass, start, reservation.getDate().with(reservation.getEndTime()), reservation.getParticipants(), reservation.getPurpose(), incursPenalty);
    }

    /**
     * Returns the booking page again, but with an error message.
     */
    private String bookingError(Model model, Integer facilityId, String message) {
        String facilityName = facilityService.getFacilityById(facilityId).map(Facility::getName).orElse("Facility");
        model.addAttribute("facilityId", facilityId);
        model.addAttribute("facilityName", facilityName);
        model.addAttribute("overlapError", message);
        return "reservation-booking";
    }
}