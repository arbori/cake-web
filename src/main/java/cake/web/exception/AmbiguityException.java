package cake.web.exception;

public class AmbiguityException extends Exception {
    public AmbiguityException(String message) {
        super(message);
    }

    public AmbiguityException(String message, Throwable cause) {
        super(message, cause);
    }
}
