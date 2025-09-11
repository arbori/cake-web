package cake.web.exchange;

public class ParameterNotFoundException extends Exception{
    public ParameterNotFoundException() {
        super();
    }

    public ParameterNotFoundException(String message) {
        super(message);
    }

    public ParameterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParameterNotFoundException(Throwable cause) {
        super(cause);
    }
}
