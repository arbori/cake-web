package cake.web.exchange;

/**
 * Enumeration representing standard HTTP request method names supported by Cake Web.
 * 
 * <p>Each enum constant maps to its lowercase convention used for resource method resolution.</p>
 * 
 * @since 1.0.0
 */
public enum HttpMethodName {
    /** HTTP GET method. */
    GET("get"),
    /** HTTP HEAD method. */
    HEAD("head"),
    /** HTTP POST method. */
    POST("post"),
    /** HTTP PUT method. */
    PUT("put"),
    /** HTTP DELETE method. */
    DELETE("delete"),
    /** HTTP CONNECT method. */
    CONNECT("connect"),
    /** HTTP OPTIONS method. */
    OPTIONS("options"),
    /** HTTP TRACE method. */
    TRACE("trace"),
    /** HTTP PATCH method. */
    PATCH("patch");

    private final String methodName;

    private HttpMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return methodName;
    }
}

