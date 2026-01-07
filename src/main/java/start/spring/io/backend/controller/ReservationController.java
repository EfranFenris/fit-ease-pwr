package start.spring.io.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import start.spring.io.backend.model.Reservation;
import start.spring.io.backend.service.ReservationService;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("reservations", service.getAll());
        model.addAttribute("newReservation", new Reservation());
        return "reservation-list";
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