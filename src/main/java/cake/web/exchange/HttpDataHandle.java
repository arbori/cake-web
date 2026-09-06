package cake.web.exchange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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

        try {
            if(targetType.isArray()) {
                return buildArray(targetType.getComponentType());
            }
            else if(targetType.getName().equals("java.util.List")) {
                return buildList(targetType);
            }
            else {
                return MAPPER.readValue(bodyContent.toString(), 
                    MAPPER.getTypeFactory().constructArrayType(targetType));

            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                """
                Cannot parse JSON as T[], List<T> or a single object of <name>.        
                """
                .replace("T", targetType.getName())
                .replace("<name>", targetType.getName())
            );
        }
    }

    private Object buildList(Class<?> elementType) throws IOException {
        return Arrays.asList(buildArray(elementType));
    }

    private Object[] buildArray(Class<?> elementType) throws IOException {
        // Try raw array first
        if (bodyContent.isArray()) {
            return MAPPER.readValue(bodyContent.toString(), 
                MAPPER.getTypeFactory().constructArrayType(elementType));
        }
        
        // Try wrapped with plural key
        String key = elementType.getSimpleName().toLowerCase() + "s";
        if (bodyContent.has(key) && bodyContent.get(key).isArray()) {
            return MAPPER.readValue(bodyContent.get(key).toString(), 
                MAPPER.getTypeFactory().constructArrayType(elementType));
        }
        
        // Try single object (wrap as array of one) - backward compatibility
        String singularKey = elementType.getSimpleName().toLowerCase();
        if (bodyContent.has(singularKey)) {
            Object singleObject = MAPPER.treeToValue(bodyContent.get(singularKey), elementType);
            return new Object[] { singleObject };
        }
        
        throw new IllegalArgumentException(
            "Cannot parse JSON as " + elementType.getSimpleName() + "[]. " +
            "Expected array (raw or wrapped with '" + key + "') or single object."
        );
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
