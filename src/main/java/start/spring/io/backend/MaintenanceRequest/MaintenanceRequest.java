package start.spring.io.backend.MaintenanceRequest;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_request")
/**
 * JPA entity mapping the "maintenance_request" table.
 * Represents a maintenance request.
 */
public class MaintenanceRequest {

    /** Auto-increment primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "requestid")
    private Integer requestId;

    /** ID of the reporting user. */
    @Column(name = "userid", nullable = false)
    private Integer userId;

    /** Affected facility. */
    @Column(name = "facilityid", nullable = false)
    private Integer facilityId;

    /** Assigned staff member (optional). */
    @Column(name = "staffid")
    private Integer staffId;

    /** Problem description. */
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Request status */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** Report date-time. */
    @Column(name = "reportdate", nullable = false)
    private LocalDateTime reportDate;

    /** Issue type (category). */
    @Column(name = "issuetype", nullable = false, length = 100)
    private String issueType;

    /** Problem severity. */
    @Column(name = "severity", nullable = false, length = 100)
    private String severity;

    /** No-args constructor required by JPA. */
    public MaintenanceRequest() {
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
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

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDateTime reportDate) {
        this.reportDate = reportDate;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
