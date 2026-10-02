package cake.web.exception;

/**
 * Thrown when a primitive type is encountered where an object wrapper or complex type is expected.
 * 
 * @since 1.0.0
 */
public class PrimitiveNotAllowedException extends ClassCastException {

    /**
     * Constructs a new PrimitiveNotAllowedException with {@code null} as its detail message.
     */
    public PrimitiveNotAllowedException() {
    }

    /**
     * Constructs a new PrimitiveNotAllowedException with the specified detail message.
     * 
     * @param s the detail message
     */
    public PrimitiveNotAllowedException(String s) {
        super(s);
    }
}

