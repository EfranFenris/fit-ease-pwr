package start.spring.io.backend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import start.spring.io.backend.model.Reservation;
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
    public String list(Model model) {
        model.addAttribute("reservations", service.getAll());
        model.addAttribute("newReservation", new Reservation());
        return "reservation-list";
    }

    @GetMapping("/new/{facilityId}")
    public String newReservation(@PathVariable Integer facilityId, Model model) {
        String facilityName = facilityService.getFacilityById(facilityId)
                .map(start.spring.io.backend.model.Facility::getName)
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
                    .map(start.spring.io.backend.model.User::getUserId)
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
}
