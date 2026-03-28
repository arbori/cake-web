package cake.web.resource;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

import cake.web.exception.FrameworkException;
import cake.web.exchange.content.Convertion;

/**
 * Base class for all resources (controllers).
 * Provides access to HTTP request data like body, headers, and params.
 */
public abstract class BaseResource {
    private String bodyContent;
    private Map<String, String[]> parameterMap = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private String authToken;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /* -----------------------------
     * Accessors for HTTP metadata
     * ----------------------------- */
    public String getRawBody() {
        return bodyContent;
    }

    public void setRawBody(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public void setParameterMap(Map<String, String[]> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public String getAuthToken() {
        return authToken;
    }

    /**
     * Populate an instance of the given type from the JSON body content.
     * @param <T> the target type
     * @param targetType the class of the target type
     * @return an instance of the target type populated from the JSON body
     */
    public <T> T getBody(Class<T> targetType) {
        if (bodyContent == null || bodyContent.isEmpty()) {
            return null;
        }

        try {
            return MAPPER.readValue(bodyContent, targetType);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to parse JSON body into " + targetType.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Populate an instance of the given header type from the request headers.
     * @param <T> the header type
     * @param targetType the class of the header type
     * @return an instance of the header type populated from request headers
     */
    public <T> T getHeader(Class<T> targetType) {
        T result;

        try {
            result = targetType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException e) {
            throw new FrameworkException(
                "Failed to create instance of header type " + targetType.getSimpleName() + ": " + e.getMessage(), e);
        }
        
        for (var field : targetType.getDeclaredFields()) {
            String headerValue = headers.get(field.getName());
            
            if (headerValue != null) {
                Convertion.trySetAttributes(field.getName(), headerValue, targetType, result);
            }
        }

        return result;
    }
}
