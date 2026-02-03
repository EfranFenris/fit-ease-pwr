package start.spring.io.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * This class is the core of the application: A Booking.
 * It connects three things:
 * 1. A User (Who booked it?)
 * 2. A Facility (Which court?)
 * 3. A Time (When is it?)
 */
@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservationid")
    private Integer reservationId;

    /**
     * The person who made the booking.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    /**
     * The specific court or field being booked.
     * We use the whole 'Facility' object here, not just an ID number.
     * This allows us to easily say "reservation.getFacility().getName()" later.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facilityid", nullable = false)
    private Facility facility;

    @Column(name = "date", nullable = false)
    private LocalDateTime date; // Full date and time (like 2023-10-15 10:00)

    @Column(name = "starttime", nullable = false)
    private LocalTime startTime; // Just the start clock time (like 10:00)

    @Column(name = "endtime", nullable = false)
    private LocalTime endTime; // Just the end clock time (like 11:30)

    @Column(name = "participants")
    private Integer participants;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose; // for example "Training match"

    public Reservation() {}

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // Getters/Setters
    public Facility getFacility() { return facility; }
    public void setFacility(Facility facility) { this.facility = facility; }

    /**
     * Helper Method
     */
    public Integer getFacilityId() { return facility != null ? facility.getFacilityId() : null; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public Integer getParticipants() { return participants; }
    public void setParticipants(Integer participants) { this.participants = participants; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
}