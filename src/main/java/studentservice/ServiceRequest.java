package studentservice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a single service request lodged by a student.
 * <p>
 * Every new request starts with the status {@link RequestStatus#PENDING}.
 * The request ID, student, service type and creation time never change;
 * only the status (and the "last updated" time) can be modified.
 */
public class ServiceRequest {

    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final String requestId;
    private final Student student;
    private final ServiceType serviceType;
    private final String description;
    private final LocalDateTime createdAt;
    private RequestStatus status;
    private LocalDateTime lastUpdated;

    /**
     * Creates a new request with status PENDING.
     *
     * @throws IllegalArgumentException if any argument is missing or invalid
     */
    public ServiceRequest(String requestId, Student student, ServiceType serviceType,
                          String description) {
        this(requestId, student, serviceType, description, RequestStatus.PENDING,
                LocalDateTime.now(), null);
    }

    /**
     * Full constructor, used when re-loading a saved request from file.
     */
    public ServiceRequest(String requestId, Student student, ServiceType serviceType,
                          String description, RequestStatus status,
                          LocalDateTime createdAt, LocalDateTime lastUpdated) {
        if (InputValidator.isBlank(requestId)) {
            throw new IllegalArgumentException("Request ID is required.");
        }
        this.student = Objects.requireNonNull(student, "Student is required.");
        this.serviceType = Objects.requireNonNull(serviceType, "Service type is required.");
        this.status = Objects.requireNonNull(status, "Status is required.");
        this.createdAt = Objects.requireNonNull(createdAt, "Creation time is required.");
        if (!InputValidator.isValidDescription(description)) {
            throw new IllegalArgumentException("Invalid description.");
        }
        this.requestId = requestId.trim().toUpperCase();
        this.description = description.trim();
        this.lastUpdated = (lastUpdated == null) ? createdAt : lastUpdated;
    }

    public String getRequestId() {
        return requestId;
    }

    public Student getStudent() {
        return student;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getDescription() {
        return description;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Changes the status of this request.
     *
     * @param newStatus the new status (must not be null)
     * @return true if the status changed, false if it already had that status
     */
    public boolean updateStatus(RequestStatus newStatus) {
        Objects.requireNonNull(newStatus, "New status is required.");
        if (newStatus == status) {
            return false;
        }
        status = newStatus;
        lastUpdated = LocalDateTime.now();
        return true;
    }

    /** Convenience check used by the GUI and tests. */
    public boolean isResolved() {
        return status == RequestStatus.RESOLVED;
    }

    /** Multi-line summary shown in the GUI details panel. */
    public String toDetailedString() {
        return "Request ID:    " + requestId + "\n"
                + "Student:       " + student.getName() + " (" + student.getStudentId() + ")\n"
                + "Email:         " + student.getEmail() + "\n"
                + "Contact:       " + student.getContactNumber() + "\n"
                + "Service Type:  " + serviceType.getDisplayName() + "\n"
                + "Status:        " + status.getDisplayName() + "\n"
                + "Created:       " + createdAt.format(DATE_FORMAT) + "\n"
                + "Last Updated:  " + lastUpdated.format(DATE_FORMAT) + "\n\n"
                + "Description:\n" + description;
    }

    @Override
    public String toString() {
        return requestId + " - " + serviceType.getDisplayName() + " - " + status.getDisplayName();
    }
}
