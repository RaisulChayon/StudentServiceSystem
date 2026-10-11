package studentservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentServiceSystemTest {

    private StudentServiceSystem system;

    @BeforeEach
    void setUp() {
        system = new StudentServiceSystem();
    }

    /** Helper that submits a valid request for the given student. */
    private ServiceRequest submitValid(String studentId) throws ValidationException {
        return system.submitRequest(studentId, "Priya Sharma", "priya@uni.edu",
                "0412345678", ServiceType.ACADEMIC_ENQUIRY, "Question about my enrolment.");
    }

    // ---------- Submit ----------

    @Test
    @DisplayName("TC25 Submit - valid request is stored with ID REQ-0001 and status Pending")
    void submitValidRequest() throws ValidationException {
        ServiceRequest r = submitValid("S1234567");
        assertEquals("REQ-0001", r.getRequestId());
        assertEquals(RequestStatus.PENDING, r.getStatus());
        assertEquals(1, system.getRequestCount());
    }

    @Test
    @DisplayName("TC26 Submit - generated Request IDs are unique and sequential")
    void requestIdsAreUnique() throws ValidationException {
        ServiceRequest first = submitValid("S1234567");
        ServiceRequest second = submitValid("S7654321");
        assertNotEquals(first.getRequestId(), second.getRequestId());
        assertEquals("REQ-0002", second.getRequestId());
    }

    @Test
    @DisplayName("TC27 Submit - invalid input throws ValidationException and nothing is stored")
    void submitInvalidRequest() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> system.submitRequest("", "Priya", "bad-email", "123",
                        null, "short"));
        assertEquals(5, ex.getErrors().size());
        assertEquals(0, system.getRequestCount());
    }

    @Test
    @DisplayName("TC28 Submit - same student twice shares one Student object")
    void sameStudentReused() throws ValidationException {
        ServiceRequest a = submitValid("S1234567");
        ServiceRequest b = submitValid("s1234567");
        assertSame(a.getStudent(), b.getStudent());
        assertEquals(1, system.getStudentCount());
    }

    // ---------- Search ----------

    @Test
    @DisplayName("TC29 Search - by Request ID finds the request (case-insensitive)")
    void searchByRequestId() throws ValidationException {
        submitValid("S1234567");
        assertTrue(system.findRequestById("req-0001").isPresent());
    }

    @Test
    @DisplayName("TC30 Search - unknown or blank Request ID returns empty")
    void searchByUnknownRequestId() throws ValidationException {
        submitValid("S1234567");
        assertFalse(system.findRequestById("REQ-9999").isPresent());
        assertFalse(system.findRequestById("").isPresent());
    }

    @Test
    @DisplayName("TC31 Search - by Student ID returns all of that student's requests")
    void searchByStudentId() throws ValidationException {
        submitValid("S1234567");
        submitValid("S7654321");
        submitValid("S1234567");
        List<ServiceRequest> results = system.findRequestsByStudentId("S1234567");
        assertEquals(2, results.size());
        assertTrue(system.findRequestsByStudentId("S0000000").isEmpty());
    }

    // ---------- Update ----------

    @Test
    @DisplayName("TC32 Update - status of existing request is changed")
    void updateStatus() throws ValidationException {
        submitValid("S1234567");
        assertTrue(system.updateRequestStatus("REQ-0001", RequestStatus.IN_PROGRESS));
        assertEquals(RequestStatus.IN_PROGRESS, system.findRequestById("REQ-0001").get().getStatus());
    }

    @Test
    @DisplayName("TC33 Update - unknown Request ID or null status throws ValidationException")
    void updateInvalid() throws ValidationException {
        submitValid("S1234567");
        assertThrows(ValidationException.class,
                () -> system.updateRequestStatus("REQ-9999", RequestStatus.RESOLVED));
        assertThrows(ValidationException.class,
                () -> system.updateRequestStatus("REQ-0001", null));
    }

    @Test
    @DisplayName("TC34 Summary - counts requests per status")
    void statusSummary() throws ValidationException {
        submitValid("S1234567");
        submitValid("S1234567");
        system.updateRequestStatus("REQ-0002", RequestStatus.RESOLVED);
        Map<RequestStatus, Integer> summary = system.getStatusSummary();
        assertEquals(1, summary.get(RequestStatus.PENDING).intValue());
        assertEquals(0, summary.get(RequestStatus.IN_PROGRESS).intValue());
        assertEquals(1, summary.get(RequestStatus.RESOLVED).intValue());
    }

    @Test
    @DisplayName("TC37 Filter - by status returns only matching requests")
    void filterByStatus() throws ValidationException {
        submitValid("S1234567");
        submitValid("S7654321");
        submitValid("S1234567");
        system.updateRequestStatus("REQ-0002", RequestStatus.IN_PROGRESS);
        assertEquals(2, system.findRequestsByStatus(RequestStatus.PENDING).size());
        assertEquals("REQ-0002",
                system.findRequestsByStatus(RequestStatus.IN_PROGRESS).get(0).getRequestId());
        assertTrue(system.findRequestsByStatus(RequestStatus.RESOLVED).isEmpty());
    }
}
