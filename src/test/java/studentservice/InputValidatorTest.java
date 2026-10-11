package studentservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests every validation rule with normal, invalid and boundary values.
 */
class InputValidatorTest {

    // ---------- Student ID (6-10 letters/digits) ----------

    @Test
    @DisplayName("TC01 Student ID - normal value is accepted")
    void studentIdNormal() {
        assertTrue(InputValidator.isValidStudentId("S1234567"));
    }

    @Test
    @DisplayName("TC02 Student ID - empty / blank / null is rejected")
    void studentIdBlank() {
        assertFalse(InputValidator.isValidStudentId(""));
        assertFalse(InputValidator.isValidStudentId("   "));
        assertFalse(InputValidator.isValidStudentId(null));
    }

    @Test
    @DisplayName("TC03 Student ID - symbols and spaces are rejected")
    void studentIdInvalidCharacters() {
        assertFalse(InputValidator.isValidStudentId("S123-456"));
        assertFalse(InputValidator.isValidStudentId("S123 456"));
    }

    @Test
    @DisplayName("TC04 Student ID - boundaries: 5 and 11 chars rejected, 6 and 10 accepted")
    void studentIdBoundaries() {
        assertFalse(InputValidator.isValidStudentId("S1234"));       // 5
        assertTrue(InputValidator.isValidStudentId("S12345"));       // 6
        assertTrue(InputValidator.isValidStudentId("S123456789"));   // 10
        assertFalse(InputValidator.isValidStudentId("S1234567890")); // 11
    }

    // ---------- Name ----------

    @Test
    @DisplayName("TC05 Name - normal names incl. hyphen and apostrophe accepted")
    void nameNormal() {
        assertTrue(InputValidator.isValidName("Priya Sharma"));
        assertTrue(InputValidator.isValidName("Mary-Jane O'Neil"));
    }

    @Test
    @DisplayName("TC06 Name - digits or symbols rejected")
    void nameInvalid() {
        assertFalse(InputValidator.isValidName("John123"));
        assertFalse(InputValidator.isValidName("Jane@Doe"));
        assertFalse(InputValidator.isValidName(""));
    }

    @Test
    @DisplayName("TC07 Name - boundaries: 1 and 51 chars rejected, 2 and 50 accepted")
    void nameBoundaries() {
        assertFalse(InputValidator.isValidName("A"));
        assertTrue(InputValidator.isValidName("Al"));
        assertTrue(InputValidator.isValidName("A".repeat(50)));
        assertFalse(InputValidator.isValidName("A".repeat(51)));
    }

    // ---------- Email ----------

    @Test
    @DisplayName("TC08 Email - normal addresses accepted")
    void emailNormal() {
        assertTrue(InputValidator.isValidEmail("priya.sharma@student.edu.au"));
        assertTrue(InputValidator.isValidEmail("a@b.co"));
    }

    @Test
    @DisplayName("TC09 Email - malformed addresses rejected")
    void emailInvalid() {
        assertFalse(InputValidator.isValidEmail("priya.sharma"));
        assertFalse(InputValidator.isValidEmail("priya@"));
        assertFalse(InputValidator.isValidEmail("@student.edu"));
        assertFalse(InputValidator.isValidEmail("priya@student"));
        assertFalse(InputValidator.isValidEmail("priya..s@student.edu"));
        assertFalse(InputValidator.isValidEmail(""));
    }

    // ---------- Contact number ----------

    @Test
    @DisplayName("TC10 Contact - mobile with spaces and international format accepted")
    void contactNormal() {
        assertTrue(InputValidator.isValidContactNumber("0412 345 678"));
        assertTrue(InputValidator.isValidContactNumber("+61412345678"));
    }

    @Test
    @DisplayName("TC11 Contact - letters or symbols rejected")
    void contactInvalid() {
        assertFalse(InputValidator.isValidContactNumber("04123abc78"));
        assertFalse(InputValidator.isValidContactNumber("0412-345-678"));
        assertFalse(InputValidator.isValidContactNumber(""));
    }

    @Test
    @DisplayName("TC12 Contact - boundaries: 7 and 16 digits rejected, 8 and 15 accepted")
    void contactBoundaries() {
        assertFalse(InputValidator.isValidContactNumber("1234567"));
        assertTrue(InputValidator.isValidContactNumber("12345678"));
        assertTrue(InputValidator.isValidContactNumber("123456789012345"));
        assertFalse(InputValidator.isValidContactNumber("1234567890123456"));
    }

    // ---------- Description ----------

    @Test
    @DisplayName("TC13 Description - boundaries: 9 and 501 rejected, 10 and 500 accepted")
    void descriptionBoundaries() {
        assertFalse(InputValidator.isValidDescription("x".repeat(9)));
        assertTrue(InputValidator.isValidDescription("x".repeat(10)));
        assertTrue(InputValidator.isValidDescription("x".repeat(500)));
        assertFalse(InputValidator.isValidDescription("x".repeat(501)));
    }

    @Test
    @DisplayName("TC14 Description - surrounding spaces are not counted")
    void descriptionTrimmed() {
        assertFalse(InputValidator.isValidDescription("   short   "));
    }

    // ---------- Whole form ----------

    @Test
    @DisplayName("TC15 Form - all fields empty gives one message per field")
    void formAllEmpty() {
        List<String> errors = InputValidator.validateRequestForm("", "", "", "", null, "");
        assertEquals(6, errors.size());
    }

    @Test
    @DisplayName("TC16 Form - valid data gives no errors")
    void formValid() {
        List<String> errors = InputValidator.validateRequestForm("S1234567", "Priya Sharma",
                "priya@student.edu.au", "0412345678", ServiceType.IT_SUPPORT,
                "Cannot log in to the student portal.");
        assertTrue(errors.isEmpty());
    }
}
