package cake.web.exception;

/**
 * Thrown when an incoming HTTP request is malformed or invalid.
 * 
 * @since 1.0.0
 */
public class BadRequestException extends BusinessException {

    /**
     * Constructs a new BadRequestException with the specified detail message.
     * 
     * @param message the detail message returned to the client
     */
    public BadRequestException(String message) {
        super(message);
    }
}

