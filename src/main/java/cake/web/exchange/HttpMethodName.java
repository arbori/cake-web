package cake.web.exchange;

public enum HttpMethodName {
    GET("get"),
    POST("post"),
    PUT("put"),
    DELETE("delete");

    private final String methodName;

    HttpMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return methodName;
    }
}
