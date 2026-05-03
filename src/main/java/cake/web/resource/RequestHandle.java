package cake.web.resource;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cake.web.exception.FrameworkException;
import cake.web.exchange.content.Convertion;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Base class for all resources (controllers).
 * Provides access to HTTP request data like body, headers, and params.
 */
public class RequestHandle {
    private List<String> pathParams;
    private List<Object> pathResources;
    private Map<String, String[]> queryParameterMap;
    private String bodyContent;
    private Map<String, String> headers;
    private String authToken;
    private JsonNode rootNode;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Constructs a RequestHandle by extracting relevant data from the
     * HttpServletRequest.
     * 
     * @param request the HttpServletRequest object containing the request data
     * @throws IOException if an I/O error occurs while reading the request body
     */
    public RequestHandle(HttpServletRequest request) throws IOException {
        // Initializa containers.
        this.pathParams = new ArrayList<>();
        this.pathResources = new ArrayList<>();
        this.queryParameterMap = new HashMap<>(request.getParameterMap());
        this.headers = extractHeaders(request);

        // Extract the auth token from the Authorization header, if present.
        this.authToken = extractAuthToken(request);

        // Get the body lines and concatenate them into a single string
        this.bodyContent = request.getReader() != null ? 
            request.getReader().lines().reduce("", (acc, line) -> acc + line + "\n").trim() : 
            null;

        // If the body content is not empty, parse it as JSON and store in rootNode
        if (this.bodyContent != null && !this.bodyContent.isEmpty()) {
            this.rootNode = MAPPER.readTree(this.bodyContent);
        }
    }

    /**
     * Builds an instance of the specified type from the JSON body content.
     * @param <T> the type of the object to build
     * @param targetType the class of the object to build
     * @return an instance of the specified type populated from the JSON body content
     */
    public <T> T buildFromBody(Class<T> targetType) {
        if (bodyContent == null || bodyContent.isEmpty()) {
            return null;
        }

        // Convert to a key (e.g., "Customer" -> "customer")
        String key = targetType.getSimpleName().toLowerCase();

        if (rootNode.has(key)) {
            try {
                // Parse only the subtree for this specific class
                return MAPPER.treeToValue(rootNode.get(key), targetType);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Failed to parse JSON body into " + targetType.getSimpleName() + ": " + e.getMessage(), e);
            }
        }

        throw new IllegalArgumentException(
                "There is no object named " + key + " in the JSON body for type " + targetType.getSimpleName() + ".");
    }

    /**
     * Populate an instance of the given header type from the request headers.
     * 
     * @param <T>        the header type
     * @param targetType the class of the header type
     * @return an instance of the header type populated from request headers
     */
    public <T> T buildFromHeader(Class<T> targetType) {
        T result;

        try {
            result = targetType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException e) {
            throw new FrameworkException(
                    "Failed to create instance of header type " + targetType.getSimpleName() + ": " + e.getMessage(),
                    e);
        }

        for (var field : targetType.getDeclaredFields()) {
            String headerValue = headers.get(field.getName());

            if (headerValue != null) {
                Convertion.trySetAttributes(field.getName(), headerValue, targetType, result);
            }
        }

        return result;
    }

    /**
     * Extracts headers from the HttpServletRequest into a Map.
     * 
     * @param request the HttpServletRequest object
     * @return a Map of header names to values
     */
    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();

        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            result.put(name, request.getHeader(name));
        }

        return result;
    }

    /**
     * Extracts the Authorization header as a Bearer token.
     * 
     * @param request the HttpServletRequest object
     * @return the extracted token, or null if not present
     */
    private String extractAuthToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");

        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }

        return auth;
    }

    // Getters and setters for the fields

    public List<String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(List<String> pathParams) {
        this.pathParams = pathParams;
    }

    public List<Object> getPathResources() {
        return pathResources;
    }

    public void setPathResources(List<Object> pathResources) {
        this.pathResources = pathResources;
    }

    public Map<String, String[]> getQueryParameterMap() {
        return queryParameterMap;
    }

    public void setQueryParameterMap(Map<String, String[]> queryParameterMap) {
        this.queryParameterMap = queryParameterMap;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
