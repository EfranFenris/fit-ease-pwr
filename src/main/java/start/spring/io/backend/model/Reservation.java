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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    // --- CAMBIO CLAVE: Usamos Objeto Facility, no Integer ---
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facilityid", nullable = false)
    private Facility facility;

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

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    // Nuevos Getters/Setters para Facility
    public Facility getFacility() { return facility; }
    public void setFacility(Facility facility) { this.facility = facility; }

    // Helper: Mantiene compatibilidad para obtener el ID desde el objeto
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