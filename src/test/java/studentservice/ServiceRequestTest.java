package studentservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceRequestTest {

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student("S1234567", "Priya Sharma", "priya@uni.edu", "0412345678");
    }

    @Test
    @DisplayName("TC21 ServiceRequest - new request starts as Pending")
    void newRequestIsPending() {
        ServiceRequest r = new ServiceRequest("REQ-0001", student, ServiceType.IT_SUPPORT,
                "Wi-Fi not working in the library.");
        assertEquals(RequestStatus.PENDING, r.getStatus());
        assertEquals(r.getCreatedAt(), r.getLastUpdated());
    }

    @Test
    @DisplayName("TC22 ServiceRequest - status can move Pending -> In Progress -> Resolved")
    void statusLifecycle() {
        ServiceRequest r = new ServiceRequest("REQ-0001", student, ServiceType.IT_SUPPORT,
                "Wi-Fi not working in the library.");
        assertTrue(r.updateStatus(RequestStatus.IN_PROGRESS));
        assertEquals(RequestStatus.IN_PROGRESS, r.getStatus());
        assertTrue(r.updateStatus(RequestStatus.RESOLVED));
        assertTrue(r.isResolved());
    }

    @Test
    @DisplayName("TC23 ServiceRequest - setting the same status reports no change")
    void sameStatusNoChange() {
        ServiceRequest r = new ServiceRequest("REQ-0001", student, ServiceType.IT_SUPPORT,
                "Wi-Fi not working in the library.");
        assertFalse(r.updateStatus(RequestStatus.PENDING));
    }

    @Test
    @DisplayName("TC24 ServiceRequest - missing student, type or short description rejected")
    void invalidConstruction() {
        assertThrows(NullPointerException.class,
                () -> new ServiceRequest("REQ-0001", null, ServiceType.IT_SUPPORT, "Valid description"));
        assertThrows(NullPointerException.class,
                () -> new ServiceRequest("REQ-0001", student, null, "Valid description"));
        assertThrows(IllegalArgumentException.class,
                () -> new ServiceRequest("REQ-0001", student, ServiceType.IT_SUPPORT, "short"));
        assertThrows(IllegalArgumentException.class,
                () -> new ServiceRequest(" ", student, ServiceType.IT_SUPPORT, "Valid description"));
    }
}
