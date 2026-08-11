package cake.web.exception;

/**
 * Base exception for all framework-related issues (infra / reflection).
 * These are translated into HTTP 500 errors.
 */
public class FrameworkException extends RuntimeException {
    public FrameworkException(String message) { super(message); }
    public FrameworkException(String message, Throwable cause) { super(message, cause); }
}
