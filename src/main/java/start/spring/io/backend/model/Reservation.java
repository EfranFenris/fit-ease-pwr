// ===============================
// Reservation.java
// ===============================
package start.spring.io.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reservation")
/**
 * JPA entity mapping the "reservation" table.
 * Represents a reservation.
 */
public class Reservation {

    /** Auto-increment primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservationid")
    private Integer reservationId;

    /** ID of the user making the reservation. */
    @Column(name = "userid", nullable = false)
    private Integer userId;

    /** ID of the facility being reserved. */
    @Column(name = "facilityid", nullable = false)
    private Integer facilityId;

    /** Reservation date-time (base date). */
    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    /** Start time of the reservation. */
    @Column(name = "starttime", nullable = false)
    private LocalTime startTime;

    /** End time of the reservation. */
    @Column(name = "endtime", nullable = false)
    private LocalTime endTime;

    /** Number of participants. */
    @Column(name = "participants", nullable = false)
    private Integer participants;

    /** Optional purpose/notes. */
    @Column(name = "purpose", length = 250)
    private String purpose;

    /** No-args constructor required by JPA. */
    public Reservation() {
    }

    public Integer getReservationId() {
        return reservationId;
    }

    public void setReservationId(Integer reservationId) {
        this.reservationId = reservationId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Integer facilityId) {
        this.facilityId = facilityId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}


