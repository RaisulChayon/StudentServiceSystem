package studentservice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manages the overall application functionality: creating, storing,
 * searching and updating service requests.
 * <p>
 * This class contains the "business logic" and has no GUI code in it,
 * which keeps it easy to unit test and means the GUI could be replaced
 * without changing how requests are managed.
 */
public class StudentServiceSystem {

    /** Prefix and width used for generated IDs, e.g. REQ-0001. */
    public static final String REQUEST_ID_PREFIX = "REQ-";
    private static final int REQUEST_ID_DIGITS = 4;

    /** Students keyed by Student ID. */
    private final Map<String, Student> students = new HashMap<>();
    /** Requests keyed by Request ID; LinkedHashMap keeps them in submission order. */
    private final Map<String, ServiceRequest> requests = new LinkedHashMap<>();
    private int nextRequestNumber = 1;

    // ------------------------------------------------------------------
    // Creating requests
    // ------------------------------------------------------------------

    /**
     * Validates the form data, creates (or updates) the student and
     * stores a new service request with a unique, generated Request ID.
     *
     * @return the newly created request
     * @throws ValidationException if any input is missing or invalid
     */
    public ServiceRequest submitRequest(String studentId, String name, String email,
                                        String contactNumber, ServiceType serviceType,
                                        String description) throws ValidationException {
        List<String> errors = InputValidator.validateRequestForm(
                studentId, name, email, contactNumber, serviceType, description);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        Student student = findOrCreateStudent(studentId, name, email, contactNumber);
        ServiceRequest request =
                new ServiceRequest(generateRequestId(), student, serviceType, description);
        requests.put(request.getRequestId(), request);
        return request;
    }

    /**
     * Returns the existing student with this ID (updating their contact
     * details to the latest values entered) or creates a new student.
     */
    private Student findOrCreateStudent(String studentId, String name, String email,
                                        String contactNumber) {
        String key = normaliseId(studentId);
        Student existing = students.get(key);
        if (existing != null) {
            existing.setName(name);
            existing.setEmail(email);
            existing.setContactNumber(contactNumber);
            return existing;
        }
        Student student = new Student(studentId, name, email, contactNumber);
        students.put(student.getStudentId(), student);
        return student;
    }

    /** Generates the next unique Request ID, e.g. REQ-0001, REQ-0002 ... */
    private String generateRequestId() {
        String id;
        do {
            id = REQUEST_ID_PREFIX + String.format("%0" + REQUEST_ID_DIGITS + "d", nextRequestNumber);
            nextRequestNumber++;
        } while (requests.containsKey(id)); // guarantees uniqueness
        return id;
    }

    // ------------------------------------------------------------------
    // Searching
    // ------------------------------------------------------------------

    /** Finds a single request by its Request ID (case-insensitive). */
    public Optional<ServiceRequest> findRequestById(String requestId) {
        if (InputValidator.isBlank(requestId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(requests.get(normaliseId(requestId)));
    }

    /** Finds every request made by the given student (case-insensitive). */
    public List<ServiceRequest> findRequestsByStudentId(String studentId) {
        List<ServiceRequest> results = new ArrayList<>();
        if (InputValidator.isBlank(studentId)) {
            return results;
        }
        String key = normaliseId(studentId);
        for (ServiceRequest request : requests.values()) {
            if (request.getStudent().getStudentId().equals(key)) {
                results.add(request);
            }
        }
        return results;
    }

    /** Returns every request that currently has the given status. */
    public List<ServiceRequest> findRequestsByStatus(RequestStatus status) {
        List<ServiceRequest> results = new ArrayList<>();
        for (ServiceRequest request : requests.values()) {
            if (request.getStatus() == status) {
                results.add(request);
            }
        }
        return results;
    }

    /** Looks up a student by ID, e.g. to auto-fill their details in the form. */
    public Optional<Student> findStudentById(String studentId) {
        if (InputValidator.isBlank(studentId)) {
            return Optional.empty();
        }
        return Optional.ofNullable(students.get(normaliseId(studentId)));
    }

    // ------------------------------------------------------------------
    // Updating
    // ------------------------------------------------------------------

    /**
     * Updates the status of an existing request.
     *
     * @return true if the status changed, false if it already had that status
     * @throws ValidationException if the request does not exist or status is missing
     */
    public boolean updateRequestStatus(String requestId, RequestStatus newStatus)
            throws ValidationException {
        if (newStatus == null) {
            throw new ValidationException("Please select a status.");
        }
        ServiceRequest request = findRequestById(requestId)
                .orElseThrow(() -> new ValidationException(
                        "No request found with ID '" + requestId + "'."));
        return request.updateStatus(newStatus);
    }

    // ------------------------------------------------------------------
    // Reporting
    // ------------------------------------------------------------------

    /** Returns all requests in the order they were submitted (read-only). */
    public List<ServiceRequest> getAllRequests() {
        return Collections.unmodifiableList(new ArrayList<>(requests.values()));
    }

    public int getRequestCount() {
        return requests.size();
    }

    public int getStudentCount() {
        return students.size();
    }

    /** Counts how many requests are in each status (used for the status bar). */
    public Map<RequestStatus, Integer> getStatusSummary() {
        Map<RequestStatus, Integer> summary = new EnumMap<>(RequestStatus.class);
        for (RequestStatus status : RequestStatus.values()) {
            summary.put(status, 0);
        }
        for (ServiceRequest request : requests.values()) {
            summary.merge(request.getStatus(), 1, Integer::sum);
        }
        return summary;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static String normaliseId(String id) {
        return id.trim().toUpperCase();
    }
}
