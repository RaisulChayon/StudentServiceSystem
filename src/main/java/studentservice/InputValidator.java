package studentservice;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Centralised input validation rules for the Student Service Management System.
 * <p>
 * Keeping every rule in one class means the GUI, the model classes and the
 * unit tests all use exactly the same definition of "valid" input.
 */
public final class InputValidator {

    // ----- Validation limits (named constants instead of "magic numbers") -----
    public static final int STUDENT_ID_MIN_LENGTH = 6;
    public static final int STUDENT_ID_MAX_LENGTH = 10;
    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 50;
    public static final int EMAIL_MAX_LENGTH = 100;
    public static final int CONTACT_MIN_DIGITS = 8;
    public static final int CONTACT_MAX_DIGITS = 15;
    public static final int DESCRIPTION_MIN_LENGTH = 10;
    public static final int DESCRIPTION_MAX_LENGTH = 500;

    private static final Pattern STUDENT_ID_PATTERN = Pattern.compile("^[A-Za-z0-9]+$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z '\\-]*$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$");
    private static final Pattern CONTACT_PATTERN = Pattern.compile("^\\+?[0-9]+$");

    /** Utility class - not meant to be instantiated. */
    private InputValidator() {
    }

    /** Returns true if the value is null, empty or only whitespace. */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /** Student ID: 6-10 letters/digits, no spaces or symbols (e.g. S1234567). */
    public static boolean isValidStudentId(String studentId) {
        if (isBlank(studentId)) {
            return false;
        }
        String trimmed = studentId.trim();
        return trimmed.length() >= STUDENT_ID_MIN_LENGTH
                && trimmed.length() <= STUDENT_ID_MAX_LENGTH
                && STUDENT_ID_PATTERN.matcher(trimmed).matches();
    }

    /** Name: 2-50 characters; letters, spaces, hyphens and apostrophes only. */
    public static boolean isValidName(String name) {
        if (isBlank(name)) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= NAME_MIN_LENGTH
                && trimmed.length() <= NAME_MAX_LENGTH
                && NAME_PATTERN.matcher(trimmed).matches();
    }

    /** Email: standard name@domain.tld format, maximum 100 characters. */
    public static boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        String trimmed = email.trim();
        return trimmed.length() <= EMAIL_MAX_LENGTH
                && EMAIL_PATTERN.matcher(trimmed).matches()
                && !trimmed.contains("..");
    }

    /**
     * Contact number: 8-15 digits, optional leading '+'.
     * Spaces are allowed for readability (e.g. "0412 345 678") and ignored.
     */
    public static boolean isValidContactNumber(String contactNumber) {
        if (isBlank(contactNumber)) {
            return false;
        }
        String compact = normaliseContactNumber(contactNumber);
        if (!CONTACT_PATTERN.matcher(compact).matches()) {
            return false;
        }
        int digitCount = compact.startsWith("+") ? compact.length() - 1 : compact.length();
        return digitCount >= CONTACT_MIN_DIGITS && digitCount <= CONTACT_MAX_DIGITS;
    }

    /** Description: 10-500 characters (after trimming). */
    public static boolean isValidDescription(String description) {
        if (isBlank(description)) {
            return false;
        }
        int length = description.trim().length();
        return length >= DESCRIPTION_MIN_LENGTH && length <= DESCRIPTION_MAX_LENGTH;
    }

    /** Removes spaces from a contact number so it is stored consistently. */
    public static String normaliseContactNumber(String contactNumber) {
        return contactNumber == null ? "" : contactNumber.replaceAll("\\s+", "");
    }

    /**
     * Validates every field of the request form and returns a list of
     * user-friendly error messages. An empty list means the form is valid.
     */
    public static List<String> validateRequestForm(String studentId, String name, String email,
                                                   String contactNumber, ServiceType serviceType,
                                                   String description) {
        List<String> errors = new ArrayList<>();

        if (isBlank(studentId)) {
            errors.add("Student ID is required.");
        } else if (!isValidStudentId(studentId)) {
            errors.add("Student ID must be " + STUDENT_ID_MIN_LENGTH + "-" + STUDENT_ID_MAX_LENGTH
                    + " letters or digits (e.g. S1234567).");
        }

        if (isBlank(name)) {
            errors.add("Name is required.");
        } else if (!isValidName(name)) {
            errors.add("Name must be " + NAME_MIN_LENGTH + "-" + NAME_MAX_LENGTH
                    + " characters and contain only letters, spaces, hyphens or apostrophes.");
        }

        if (isBlank(email)) {
            errors.add("Email address is required.");
        } else if (!isValidEmail(email)) {
            errors.add("Email address is not valid (e.g. name@example.com).");
        }

        if (isBlank(contactNumber)) {
            errors.add("Contact number is required.");
        } else if (!isValidContactNumber(contactNumber)) {
            errors.add("Contact number must contain " + CONTACT_MIN_DIGITS + "-" + CONTACT_MAX_DIGITS
                    + " digits (an optional leading + is allowed).");
        }

        if (serviceType == null) {
            errors.add("Please select a service type.");
        }

        if (isBlank(description)) {
            errors.add("Request description is required.");
        } else if (!isValidDescription(description)) {
            errors.add("Description must be between " + DESCRIPTION_MIN_LENGTH + " and "
                    + DESCRIPTION_MAX_LENGTH + " characters (currently "
                    + description.trim().length() + ").");
        }

        return errors;
    }
}
