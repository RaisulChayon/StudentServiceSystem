package studentservice;

/**
 * Represents a student who makes service requests.
 * First version: stores the details with basic "not empty" checks.
 */
public class Student {

    private final String studentId;
    private String name;
    private String email;
    private String contactNumber;

    public Student(String studentId, String name, String email, String contactNumber) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required.");
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
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
        this.name = name.trim();
    }

    public final void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        this.email = email.trim();
    }

    public final void setContactNumber(String contactNumber) {
        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact number is required.");
        }
        this.contactNumber = contactNumber.trim();
    }

    @Override
    public String toString() {
        return name + " (" + studentId + ")";
    }
}
