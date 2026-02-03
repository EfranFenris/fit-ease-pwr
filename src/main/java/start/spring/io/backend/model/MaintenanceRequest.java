package start.spring.io.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * This class represents a report filed when something is broken.
 * It links a User (who reported it) to a Facility (what is broken).
 */
@Entity
@Table(name = "maintenance_request")
public class MaintenanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "requestid")
    private Integer requestId;

    /**
     * @ManyToOne means "Many requests can be created by One User".
     * FetchType.EAGER means when we load this request from the database,
     * we want Java to automatically load the User details (name, email) too,
     * because we need to know who sent it.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    /**
     * Links the report to the specific court that needs fixing.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facilityid", nullable = false)
    private Facility facility;

    /**
     * Optionally, links to a maintenance staff member who is assigned to fix it.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staffid")
    private User staff;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // e.g., "PENDING", "IN_PROGRESS", "FIXED"

    @Column(name = "reportdate", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime reportDate;

    @Column(name = "issuetype", nullable = false, length = 100)
    private String issueType;

    @Column(name = "severity", nullable = false, length = 100)
    private String severity;

    public MaintenanceRequest() {}

    // Getters and Setters

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Facility getFacility() { return facility; }
    public void setFacility(Facility facility) { this.facility = facility; }

    public User getStaff() { return staff; }
    public void setStaff(User staff) { this.staff = staff; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getReportDate() { return reportDate; }
    public void setReportDate(LocalDateTime reportDate) { this.reportDate = reportDate; }

    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
}