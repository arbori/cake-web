package cake.web.exception;

/** Example subclasses */
public class NotFoundException extends BusinessException {
    public NotFoundException(String message) { super(message); }

    public NotFoundException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }
}
