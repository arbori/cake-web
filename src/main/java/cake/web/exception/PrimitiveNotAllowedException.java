package cake.web.exception;

public class PrimitiveNotAllowedException extends ClassCastException {
    public PrimitiveNotAllowedException() {
    }

    public PrimitiveNotAllowedException(String s) {
        super(s);
    }
}
