package start.spring.io.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * This class represents a "Strike" or "Penalty".
 * If a user breaks the rules (like not showing up for a match), we create a record here.
 * This helps the manager track who is misusing the system.
 */
@Entity
@Table(name = "penalty")
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "penaltyid")
    private Integer penaltyId;

    /**
     * Relationship: Many Penalties -> One User.
     * We link this penalty to the specific User object so we know who is in trouble.
     * FetchType.EAGER means "When you load the penalty, load the user details immediately."
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description; // like "No-show for reservation on Monday"

    @Column(name = "datehour", nullable = false)
    private LocalDateTime datehour; // When was this penalty given?

    public Penalty() {
    }

    /**
     * Constructor to easily create a new Penalty in our Controller.
     */
    public Penalty(User user, String description, LocalDateTime datehour) {
        this.user = user;
        this.description = description;
        this.datehour = datehour;
    }

    public Integer getPenaltyId() { return penaltyId; }
    public void setPenaltyId(Integer penaltyId) { this.penaltyId = penaltyId; }

    // Getters/Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDatehour() { return datehour; }
    public void setDatehour(LocalDateTime datehour) { this.datehour = datehour; }
}