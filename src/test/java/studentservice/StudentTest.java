package studentservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentTest {

    @Test
    @DisplayName("TC17 Student - constructor stores normalised values")
    void constructorStoresValues() {
        Student s = new Student(" s1234567 ", "  Priya Sharma ", "Priya@Student.EDU.au", "0412 345 678");
        assertEquals("S1234567", s.getStudentId());
        assertEquals("Priya Sharma", s.getName());
        assertEquals("priya@student.edu.au", s.getEmail());
        assertEquals("0412345678", s.getContactNumber());
    }

    @Test
    @DisplayName("TC18 Student - invalid data throws IllegalArgumentException")
    void constructorRejectsInvalidData() {
        assertThrows(IllegalArgumentException.class,
                () -> new Student("S1", "Priya", "priya@uni.edu", "0412345678"));
        assertThrows(IllegalArgumentException.class,
                () -> new Student("S1234567", "Priya", "not-an-email", "0412345678"));
    }

    @Test
    @DisplayName("TC19 Student - setter rejects invalid email and keeps old value")
    void setterKeepsOldValueOnError() {
        Student s = new Student("S1234567", "Priya", "priya@uni.edu", "0412345678");
        assertThrows(IllegalArgumentException.class, () -> s.setEmail("bad"));
        assertEquals("priya@uni.edu", s.getEmail());
    }

    @Test
    @DisplayName("TC20 Student - two objects with the same ID are equal")
    void equalityById() {
        Student a = new Student("S1234567", "Priya", "priya@uni.edu", "0412345678");
        Student b = new Student("s1234567", "Priya S", "p@uni.edu", "0499999999");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
