package cake.web.exception;

/**
 * Thrown when an HTTP method cannot be found or invoked properly.
 */
public class MethodInvocationException extends FrameworkException {
    public MethodInvocationException(String message, Throwable cause) { super(message, cause); }
}
