package start.spring.io.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "penalty")
public class Penalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "penaltyid")
    private Integer penaltyId;

    // CAMBIO: Relación directa con User
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "datehour", nullable = false)
    private LocalDateTime datehour;

    public Penalty() {
    }

    // Constructor actualizado
    public Penalty(User user, String description, LocalDateTime datehour) {
        this.user = user;
        this.description = description;
        this.datehour = datehour;
    }

    public Integer getPenaltyId() { return penaltyId; }
    public void setPenaltyId(Integer penaltyId) { this.penaltyId = penaltyId; }

    // Getters/Setters actualizados
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDatehour() { return datehour; }
    public void setDatehour(LocalDateTime datehour) { this.datehour = datehour; }
}