package cake.web.exception;

/**
 * Thrown when a resource class cannot be resolved (missing, bad constructor, etc.).
 */
public class ResourceResolutionException extends FrameworkException {
    public ResourceResolutionException(String message, Throwable cause) { super(message, cause); }
}
