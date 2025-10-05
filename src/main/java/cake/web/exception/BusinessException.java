package cake.web.exception;

/**
 * Base for business errors thrown inside user resource classes.
 * These are translated into 4xx responses.
 */
public abstract class BusinessException extends RuntimeException {
    protected BusinessException(String message) { super(message); }
}