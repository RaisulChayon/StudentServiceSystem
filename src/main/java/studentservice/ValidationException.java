package studentservice;

import java.util.Collections;
import java.util.List;

/**
 * Thrown when user input fails validation.
 * Carries every validation error so the GUI can show them all at once,
 * rather than making the user fix problems one at a time.
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(String.join(System.lineSeparator(), errors));
        this.errors = Collections.unmodifiableList(errors);
    }

    public ValidationException(String error) {
        this(List.of(error));
    }

    public List<String> getErrors() {
        return errors;
    }
}
