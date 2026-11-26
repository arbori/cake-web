package cake.web.exception;

/**
 * Thrown when a required request parameter is not found.
 * 
 * @since 1.0.0
 */
public class ParameterNotFoundException extends BusinessException {

    /**
     * Constructs a new ParameterNotFoundException with the specified detail message.
     * 
     * @param message the detail message
     */
    public ParameterNotFoundException(String message) {
        super(message);
    }
}

