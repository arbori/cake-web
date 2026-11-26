package cake.web.exception;

/**
 * Thrown when an error occurs during HTTP request exchange processing or resource binding.
 * 
 * @since 1.0.0
 */
public class ResourceExchangeException extends Exception {

    /**
     * Constructs a new ResourceExchangeException with {@code null} as its detail message.
     */
    public ResourceExchangeException() {
    }

    /**
     * Constructs a new ResourceExchangeException with the specified detail message.
     * 
     * @param message the detail message
     */
    public ResourceExchangeException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceExchangeException with the specified cause.
     * 
     * @param cause the cause
     */
    public ResourceExchangeException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new ResourceExchangeException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public ResourceExchangeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new ResourceExchangeException with the specified detail message, cause,
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     * 
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public ResourceExchangeException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

