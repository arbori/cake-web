package cake.web.exchange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


import cake.web.exception.FrameworkException;
import cake.web.exchange.content.Convertion;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 */
public class HttpDataHandle {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpServletRequest request;
    private final Map<String, String[]> queryParameterMap;
    private final Map<String, String> headers;
    private final JsonNode bodyContent;
    private final String authToken;

    /**
     * Constructs a RequestHandle by extracting relevant data from the
     * HttpServletRequest.
     * 
     * @param request the HttpServletRequest object containing the request data
     * @throws IOException if an I/O error occurs while reading the request body
     */
    public HttpDataHandle(HttpServletRequest request) throws IOException {
        this.request = request;

        // Initializa containers.
        this.queryParameterMap = request.getParameterMap();
        this.headers = extractHeaders();
        this.bodyContent = extractBodyContent();
        this.authToken = extractAuthToken();
    }

    /**
     * Builds an instance of the specified type from the JSON body content.
     * @param <T> the type of the object to build
     * @param targetType the class of the object to build
     * @return an instance of the specified type populated from the JSON body content
     * @throws IOException if an I/O error occurs while reading the request body
     * @throws IllegalArgumentException if the body content cannot be parsed into the target type
     */
    public Object buildFromBody(Class<?> targetType) throws IOException {
        // There is no body content, so the result is null (e.g., for GET requests)
        if(bodyContent == null) {
            return null;
        }

        // Convert to a key (e.g., "Customer" -> "customer")
        String key = targetType.getSimpleName();
        key = Character.toLowerCase(key.charAt(0)) + key.substring(1);

        if (bodyContent.has(key)) {
            try {
                // Parse only the subtree for this specific class
                return MAPPER.treeToValue(bodyContent.get(key), targetType);
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
    public Object buildFromHeader(Class<?> targetType) {
        Object result;

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

            // In case that header attribute start with uppercase letter.
            if(headerValue == null) {
                headerValue = headers.get(
                    field.getName().substring(0, 1).toUpperCase() + 
                    field.getName().substring(1));
            }

            if (headerValue != null && !headerValue.isEmpty()) {
                trySetAttributes(field.getName(), headerValue, targetType, result);
            }
        }

        return result;
    }

    /**
     * Populate an instance of the given header type from the request headers.
     * 
     * @param <T>        the header type
     * @param targetType the class of the header type
     * @return an instance of the header type populated from request headers
     */
    public Object buildFromQueryParameter(Class<?> targetType) {
        Object object;

        try {
            object = targetType.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException e) {
            throw new FrameworkException(
                    "Failed to create instance of query parameter type " + targetType.getSimpleName() + ": " + e.getMessage(),
                    e);
        }

        for (var field : targetType.getDeclaredFields()) {
            String[] queryParam = queryParameterMap.get(field.getName());

            if (queryParam != null && queryParam[0] != null) {
                String value = !queryParam[0].isEmpty() ? queryParam[0] : null;

                trySetAttributes(field.getName(), value, targetType, object);
            }
        }

        return object;
    }

    /**
     * Get the authorization token.
     * 
     * @return The authorization token.
     */
    public String getAuthToken() {
        return this.authToken;
    }

    /**
     * Extracts headers from the HttpServletRequest into a Map.
     * 
     * @param request the HttpServletRequest object
     * @return a Map of header names to values
     */
    private Map<String, String> extractHeaders() {
        Map<String, String> result = new HashMap<>();
        Enumeration<String> names = request.getHeaderNames();

        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            result.put(name, request.getHeader(name));
        }

        return result;
    }

    /**
     * Read the body content and convert it to a JSON node.
     * 
     * @param request the HttpServletRequest object
     * @return a JSON node representing the body content
     * @throws IOException if an I/O error occurs while reading the request body
     */
    private JsonNode extractBodyContent() throws IOException {
        // Get the body lines and concatenate them into a single string
        String body = request.getReader() != null ? 
            request.getReader().lines().reduce("", (acc, line) -> acc + line + "\n").trim() : 
            null;

        // If the body content is not empty, parse it as JSON and store in rootNode
        if (body != null && !body.isEmpty()) {
            return MAPPER.readTree(body);
        } else {
            return null;
        }
    }

    /**
     * Extracts the Authorization header as a Bearer token.
     * @param request the HttpServletRequest object
     * @return the extracted token, or null if not present
     */
    private String extractAuthToken() {
        String auth = request.getHeader("Authorization");
        
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        
        return auth;
    }

    /**
     * Tries to set a single attribute on the given instance by name and value.
     * It first attempts to find a setter method, then falls back to direct field
     * access.
     * 
     * @param name     the attribute name
     * @param value    the attribute value as string
     * @param clazz    the class of the instance
     * @param instance the object instance to set the attribute on
     */
    private void trySetAttributes(String name, Object value, Class<?> clazz, Object instance) {
        String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);

        // try setter methods first
        try {
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equalsIgnoreCase(setterName) || m.getParameterCount() != 1) {
                    continue;
                }

                Class<?> paramType = m.getParameterTypes()[0];
                Object converted = Convertion.convert(value, paramType);
                m.invoke(instance, converted);

                return;
            }
        } catch (Exception _) {
            // No setter found, fallback to field
        }
    }
}
