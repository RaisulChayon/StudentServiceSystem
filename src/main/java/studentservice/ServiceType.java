package studentservice;

/**
 * The types of service a student can request.
 * Using an enum (instead of free text) guarantees that only valid
 * service types can ever be stored against a request.
 */
public enum ServiceType {
    ACADEMIC_ENQUIRY("Academic Enquiry"),
    IT_SUPPORT("IT Support"),
    ASSESSMENT_SUPPORT("Assessment Support"),
    GENERAL_ENQUIRY("General Enquiry");

    private final String displayName;

    ServiceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Shown in the GUI drop-down list. */
    @Override
    public String toString() {
        return displayName;
    }
}
