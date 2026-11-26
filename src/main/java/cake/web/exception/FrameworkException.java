package cake.web.exception;

/**
 * Base exception for all framework-related issues (infrastructure / reflection).
 * These are translated into HTTP 500 internal server errors.
 * 
 * @since 1.0.0
 */
public class FrameworkException extends RuntimeException {

    /**
     * Constructs a new FrameworkException with the specified detail message.
     * 
     * @param message the detail message
     */
    public FrameworkException(String message) {
        super(message);
    }

    /**
     * Constructs a new FrameworkException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}

