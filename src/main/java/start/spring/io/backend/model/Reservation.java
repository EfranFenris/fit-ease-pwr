package start.spring.io.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservationid")
    private Integer reservationId;

    // CAMBIO IMPORTANTE: Relación con User
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @Column(name = "facilityid", nullable = false)
    private Integer facilityId;

    // ... resto de campos iguales ...
    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "starttime", nullable = false)
    private LocalTime startTime;

    @Column(name = "endtime", nullable = false)
    private LocalTime endTime;

    @Column(name = "participants")
    private Integer participants;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    public Reservation() {}

    // Getters y Setters Actualizados
    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // Helper para mantener compatibilidad si algún sitio pide getUserId()
    public Integer getUserId() { return user != null ? user.getUserId() : null; }

    public Integer getFacilityId() { return facilityId; }
    public void setFacilityId(Integer facilityId) { this.facilityId = facilityId; }

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