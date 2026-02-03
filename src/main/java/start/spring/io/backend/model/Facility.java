package start.spring.io.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a physical space, like "Tennis Court 1" or "Main Soccer Field".
 * It stores info about what type of sport it is and if it's currently open (Available) or closed.
 */
@Entity
@Table(name = "facility")
public class Facility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "facilityid")
    private Integer facilityId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type; // example: "Tennis", "Padel", "Football"

    @Column(name = "status", nullable = false)
    private String status; // example: "Available", "Unavailable"

    /**
     * A list of all bookings ever made for this specific court.
     */
    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();

    /**
     * A list of all broken equipment reports for this court.
     */
    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MaintenanceRequest> maintenanceRequests = new ArrayList<>();

    public Facility() {}

    public Facility(String name, String type, String status) {
        this.name = name;
        this.type = type;
        this.status = status;
    }

    // Getters and Setters

    public Integer getFacilityId() { return facilityId; }
    public void setFacilityId(Integer facilityId) { this.facilityId = facilityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<Reservation> getReservations() { return reservations; }
    public List<MaintenanceRequest> getMaintenanceRequests() { return maintenanceRequests; }
}