package cake.web.exception;

/**
 * <p>
 * Base for business errors thrown inside user resource classes.
 * </p>
 *
 * <p>
 * <b>Contract:</b> the message passed to this exception is sent
 * verbatim to the HTTP client. It MUST NOT contain internal details
 * (SQL, stack traces, class names, file paths). Write it as if the
 * client will read it — because they will.
 * </p>
 * 
 * @since 1.0.0
 */
public abstract class BusinessException extends RuntimeException {

    /**
     * Constructs a new BusinessException with the specified detail message.
     * 
     * @param message the detail message returned verbatim to the HTTP client
     */
    protected BusinessException(String message) {
        super(message);
    }
}