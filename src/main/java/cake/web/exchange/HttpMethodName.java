package cake.web.exchange;

public enum HttpMethodName {
    GET("get"),
    HEAD("head"),
    POST("post"),
    PUT("put"),
    DELETE("delete"),
    CONNECT("connect"),
    OPTIONS("options"),
    TRACE("trace"),
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
