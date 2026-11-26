package cake.web.exception;

/**
 * Thrown when an HTTP method cannot be found or invoked properly on a resource class.
 * 
 * @since 1.0.0
 */
public class MethodInvocationException extends Exception {

    /**
     * Constructs a new MethodInvocationException with {@code null} as its detail message.
     */
    public MethodInvocationException() {
    }

    /**
     * Constructs a new MethodInvocationException with the specified detail message.
     * 
     * @param message the detail message
     */
    public MethodInvocationException(String message) {
        super(message);
    }

    /**
     * Constructs a new MethodInvocationException with the specified cause.
     * 
     * @param cause the cause
     */
    public MethodInvocationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new MethodInvocationException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause
     */
    public MethodInvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new MethodInvocationException with the specified detail message, cause,
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     * 
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public MethodInvocationException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

