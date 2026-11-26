package cake.web.exception;

/**
 * Thrown when multiple resource methods match the same request signature or criteria.
 * 
 * @since 1.0.0
 */
public class AmbiguityException extends Exception {

    /**
     * Constructs a new AmbiguityException with the specified detail message.
     * 
     * @param message the detail message
     */
    public AmbiguityException(String message) {
        super(message);
    }

    /**
     * Constructs a new AmbiguityException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public AmbiguityException(String message, Throwable cause) {
        super(message, cause);
    }
}

