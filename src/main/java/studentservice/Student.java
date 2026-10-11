package studentservice;

import java.util.Objects;

/**
 * Represents a student who makes service requests.
 * <p>
 * The Student ID is fixed once the object is created (it identifies the
 * student), while contact details can be updated through validated setters.
 */
public class Student {

    private final String studentId;
    private String name;
    private String email;
    private String contactNumber;

    /**
     * Creates a new student after validating every field.
     *
     * @throws IllegalArgumentException if any field is missing or invalid
     */
    public Student(String studentId, String name, String email, String contactNumber) {
        if (!InputValidator.isValidStudentId(studentId)) {
            throw new IllegalArgumentException("Invalid student ID: " + studentId);
        }
        this.studentId = studentId.trim().toUpperCase();
        setName(name);
        setEmail(email);
        setContactNumber(contactNumber);
    }

    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public final void setName(String name) {
        if (!InputValidator.isValidName(name)) {
            throw new IllegalArgumentException("Invalid name: " + name);
        }
        this.name = name.trim();
    }

    public final void setEmail(String email) {
        if (!InputValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address: " + email);
        }
        this.email = email.trim().toLowerCase();
    }

    public final void setContactNumber(String contactNumber) {
        if (!InputValidator.isValidContactNumber(contactNumber)) {
            throw new IllegalArgumentException("Invalid contact number: " + contactNumber);
        }
        this.contactNumber = InputValidator.normaliseContactNumber(contactNumber);
    }

    /** Two Student objects are the same student if their IDs match. */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Student)) {
            return false;
        }
        return studentId.equals(((Student) other).studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }

    @Override
    public String toString() {
        return name + " (" + studentId + ")";
    }
}
