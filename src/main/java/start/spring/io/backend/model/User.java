package start.spring.io.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a "User" in our system.
 * The @Entity annotation tells Spring/Hibernate that objects of this class
 * should be saved in the database.
 * The @Table(name = "users") tells it to look for a table named "users".
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * @Id marks this field as the Primary Key (unique identifier).
     * @GeneratedValue means the database will automatically create the number
     * (1, 2, 3...) so we don't have to invent it ourselves.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userid")
    private Integer userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    /**
     * @OneToMany means "One User has Many Reservations".
     * mappedBy = "user" tells Java to look at the 'user' field inside the Reservation class to find the link.
     * @JsonIgnore is used to stop an infinite loop when converting to text (User -> Reservation -> User -> Reservation...).
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();

    /**
     * A list of all penalties/strikes assigned to this user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Penalty> penalties = new ArrayList<>();

    /**
     * A list of all maintenance reports this user has submitted.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaintenanceRequest> maintenanceRequests = new ArrayList<>();

    /**
     * Default constructor.
     * JPA (the database tool) needs an empty constructor to work properly.
     */
    public User() {}

    /**
     * Constructor for creating a new user easily in our code.
     */
    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Standard methods to access and change the private variables above.

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<Reservation> getReservations() { return reservations; }
    public List<Penalty> getPenalties() { return penalties; }
}