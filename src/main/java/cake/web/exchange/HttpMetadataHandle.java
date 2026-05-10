package cake.web.exchange;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cake.web.exception.FrameworkException;
import cake.web.exchange.content.BodyContent;
import cake.web.exchange.content.Convertion;
import cake.web.exchange.content.HeaderContent;
import cake.web.exchange.content.ResourceFilter;

/**
 * Base class for all resources (controllers).
 * Provides access to HTTP request data like body, headers, and params.
 */
public class HttpMetadataHandle {
    private HttpServletRequest request;

    private Map<String, String[]> parameterMap;
    
    private Map<String, String> headers;

    private JsonNode rootNode;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Constructs a RequestHandle by extracting relevant data from the
     * HttpServletRequest.
     * 
     * @param request the HttpServletRequest object containing the request data
     * @throws IOException if an I/O error occurs while reading the request body
     */
    public HttpMetadataHandle(HttpServletRequest request) throws IOException {
        this.request = request;

        // Initializa containers.
        this.parameterMap = new HashMap<>(request.getParameterMap());
        this.headers = extractHeaders();

        // Get the body lines and concatenate them into a single string
        String bodyContent = request.getReader() != null ? 
            request.getReader().lines().reduce("", (acc, line) -> acc + line + "\n").trim() : 
            null;

        // If the body content is not empty, parse it as JSON and store in rootNode
        if (bodyContent != null && !bodyContent.isEmpty()) {
            this.rootNode = MAPPER.readTree(bodyContent);
        } else {
            this.rootNode = null;
        }
    }

    /**
     * Inspects the resource object for fields of type BodyContent and attempts to set them
     * using the JSON body content. It looks for setter methods corresponding to the field names
     * and invokes them with the converted body content.
     * 
     * @param resource the resource object to populate with body content
     */
    public void setResourceMetaData(Object resource) {
        List<Field> fields = Arrays.asList(resource.getClass().getDeclaredFields()).stream()
            .filter(this::isMetadataHandled)
            .toList();

        for(Field field: fields) {
            Method setMethod = Arrays.asList(resource.getClass().getMethods())
                .stream()
                .filter(m -> m.getName().equalsIgnoreCase("set" + field.getName().toLowerCase()) && 
                    m.getParameterCount() == 1 &&
                    java.lang.reflect.Modifier.isPublic(m.getModifiers()) &&
                    !java.lang.reflect.Modifier.isStatic(m.getModifiers()))
                .findFirst()
                .orElse(null);

            if(setMethod == null) {
                continue;
            }

            if(Arrays.asList(field.getType().getInterfaces()).contains(BodyContent.class)) {
                setBodyContent(resource, setMethod, field);
            } else if(Arrays.asList(field.getType().getInterfaces()).contains(ResourceFilter.class)) {
                setResourceFilter(resource, setMethod, field);
            } else if(Arrays.asList(field.getType().getInterfaces()).contains(HeaderContent.class)) {
                setHeaderContent(resource, setMethod, field);
            }
        }
    }

    /**
     * Checks if the field is of a type that should be handled as metadata.
     * @param field the field to check
     * @return true if the field is of a metadata type, false otherwise
     */
    private boolean isMetadataHandled(Field field) {
        return Arrays.asList(field.getType().getInterfaces()).contains(BodyContent.class) || 
            Arrays.asList(field.getType().getInterfaces()).contains(ResourceFilter.class) ||
            Arrays.asList(field.getType().getInterfaces()).contains(HeaderContent.class);
    }

    /**
     * Sets the header content for a field of type HeaderContent by extracting values from request headers.
     * It looks for a setter method corresponding to the field name and invokes it with the converted header content.
     * 
     * @param resource the resource object containing the field
     * @param setMethod the setter method to invoke for setting the header content
     * @param field the field to set
     */
    private void setHeaderContent(Object resource, Method setMethod, Field field) {
        try {
            setMethod.invoke(resource, this.buildFromHeader(field.getType()));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new FrameworkException(
                    "Failed to set header content on resource " + resource.getClass().getSimpleName() + " via setter: " + e.getMessage(), e);
        }
    }

    /**
     * Sets the body content for a field of type BodyContent by parsing the JSON body content.
     * It looks for a setter method corresponding to the field name and invokes it with the converted body content.
     * 
     * @param resource the resource object containing the field
     * @param setMethod the setter method to invoke for setting the body content
     * @param field the field to set
     */
    private void setBodyContent(Object resource, Method setMethod, Field field) {
        try {
            setMethod.invoke(resource, buildFromBody(field.getType()));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e) {
            throw new FrameworkException(
                    "Failed to set body content on resource " + resource.getClass().getSimpleName() + " via setter: " + e.getMessage(), e);
        }
    }

    /**
     * Sets the resource filter for a field of type ResourceFilter.
     * @param resource the resource object containing the field
     * @param setMethod the setter method to invoke for setting the resource filter
     * @param field the field to set
     */
    private void setResourceFilter(Object resource, Method setMethod, Field field) {
        try {
            setMethod.invoke(resource, this.buildFromQueryParameter(field.getType()));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new FrameworkException(
                    "Failed to set resource filter on resource " + resource.getClass().getSimpleName() + " via setter: " + e.getMessage(), e);
        }
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
        if(rootNode == null) {
            return null;
        }

        // Convert to a key (e.g., "Customer" -> "customer")
        String key = targetType.getSimpleName();
        key = Character.toLowerCase(key.charAt(0)) + key.substring(1);

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
            String[] queryParam = parameterMap.get(field.getName());

            if (queryParam != null && queryParam[0] != null) {
                String value = !queryParam[0].isEmpty() ? queryParam[0] : null;

                trySetAttributes(field.getName(), value, targetType, object);
            }
        }

        return object;
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
     * Extracts the Authorization header as a Bearer token.
     * 
     * @param request the HttpServletRequest object
     * @return the extracted token, or null if not present
     */
    protected String extractAuthToken() {
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
        String setterName = "set" + name;

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
