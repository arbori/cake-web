package cake.web.exchange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import cake.web.exception.FrameworkException;
import cake.web.exchange.content.Conversion;
import cake.web.exchange.content.StyleCase;
import tools.jackson.databind.JavaType;
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

    private final boolean bodyContentAnArray;

    /**
     * Constructs a RequestHandle by extracting relevant data from the
     * HttpServletRequest.
     * 
     * @param request the HttpServletRequest object containing the request data
     * @throws IOException if an I/O error occurs while reading the request body
     */
    public HttpDataHandle(HttpServletRequest request) throws IOException {
        this.request = request;

        // Initialize containers.
        this.queryParameterMap = request.getParameterMap();
        this.headers = extractHeaders();
        this.bodyContent = extractBodyContent();
        this.authToken = extractAuthToken();

        this.bodyContentAnArray = this.bodyContent != null && this.bodyContent.isArray();

    }

    /**
     * To remove method parameters ambiguities, the framework need to know if the
     * body is or do not an array.
     * 
     * @return Return true if the body content is an array.
     */
    public boolean isBodyContentAnArray() {
        return this.bodyContentAnArray;
    }

    /**
     * Builds an instance of the specified type with the JSON body content.
     * 
     * @param targetType the class of the object to build
     * @return an instance of the specified type populated with the JSON body
     *         content
     * @throws IOException              if an I/O error occurs while reading the
     *                                  request body
     * @throws IllegalArgumentException if the body content cannot be parsed into
     *                                  the target type
     */
    public Object buildFromBody(Class<?> targetType) throws IOException {
        // There is no body content, so the result is null (e.g., for GET requests)
        if (bodyContent == null) {
            return null;
        }

        // Convert to a key (e.g., "Customer" -> "customer")
        String key = StyleCase.toCamelCase(targetType.getSimpleName());

        if (!bodyContent.has(key)) {
            // Fallback check for snake_case: "customer_request"
            key = StyleCase.toSnakeCase(targetType.getSimpleName());
        }
        
        if (!bodyContent.has(key)) {
            // Fallback check for kebab_case: "customer-request"
            key = StyleCase.toKebabCase(targetType.getSimpleName());
        }
        
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
                "Cannot parse JSON as " + targetType.getSimpleName() + ". " +
                        "Expected object wrapped with '" + key + "'.");
    }

    /**
     * Converts the HTTP body content stored in {@code bodyContent} (when structured as a JSON array)
     * into a runtime-typed Java array compatible with the provided target class.
     * <p>
     * This method is used by reflection-based frameworks to populate method arguments
     * that expect an object array (e.g., {@code AddressRequest[]}).
     *
     * @param targetClass The target class, which must be an array type (e.g., {@code AddressRequest[].class}).
     * @return An instance of the correctly typed array containing the deserialized data, 
     *         or an empty array of the same component type if the content is null or incompatible.
     * @throws IllegalArgumentException If an error occurs during JSON conversion to the specified array.
     */
    public Object buildArrayFromBody(Class<?> targetClass) {
        if (targetClass == null || !targetClass.isArray()) {
            return null; 
        }

        if (bodyContent == null || !bodyContent.isArray()) {
            // Retorna um array vazio do tipo correto caso o body venha vazio
            return java.lang.reflect.Array.newInstance(targetClass.getComponentType(), 0);
        }

        try {
            // O convertValue converte o JsonNode diretamente para o array tipado (ex: AddressRequest[])
            return MAPPER.convertValue(bodyContent, targetClass);

        } catch (Exception e) {
            Class<?> targetComponentClass = targetClass.getComponentType();
            throw new IllegalArgumentException("Cannot parse JSON as array of " + (targetComponentClass != null ? targetComponentClass.getName() : "unknown"), e);
        }
    }

    /**
     * Converts the HTTP body content stored in {@code bodyContent} (when structured as a JSON array)
     * into a runtime-typed {@link java.util.List} based on the provided generic type.
     * <p>
     * This method is used by reflection-based frameworks to populate method arguments
     * that expect generic collections (e.g., {@code List<AddressRequest>}).
     *
     * @param targetType The parameterized type ({@link java.lang.reflect.ParameterizedType}) 
     *                   representing the list and its generic argument (e.g., {@code List<AddressRequest>}).
     * @return A {@link java.util.List} containing the properly deserialized and typed elements, 
     *         or an empty list if the type is not supported or the body is invalid.
     * @throws IllegalArgumentException If the provided type is not a valid parameterized type 
     *                                  or if an error occurs during JSON conversion.
     */
    public Object buildListFromBody(Type targetType) {
        if (!(targetType instanceof ParameterizedType parameterizedType)) {
            return Collections.emptyList();
        }

        if (bodyContent == null || !bodyContent.isArray()) {
            return Collections.emptyList();
        }

        try {
            Type actualTypeArg = parameterizedType.getActualTypeArguments()[0];
            Class<?> elementClass = (Class<?>) actualTypeArg;

            // Constrói o tipo de coleção do Jackson
            JavaType listType = MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass);

            // O convertValue também aceita o JavaType para converter o JsonNode em List tipada
            return MAPPER.convertValue(bodyContent, listType);

        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot parse JSON as List<" + targetType.getTypeName() + ">", e);
        }
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
            // 1. Direct match ("e.g.: xRequestId")
            String headerValue = headers.get(field.getName());

            // 2. Kebab-case match ("e.g.: x-request-id")
            if (headerValue == null) {
                headerValue = headers.get(StyleCase.toKebabCase(field.getName()));
            }

            // 3. Train-case match ("e.g.: X-Request-Id")
            if (headerValue == null) {
                headerValue = headers.get(StyleCase.toTrainCase(field.getName()));
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
                    "Failed to create instance of query parameter type " + targetType.getSimpleName() + ": "
                            + e.getMessage(),
                    e);
        }

        for (var field : targetType.getDeclaredFields()) {
            // Check exact camelCase, snake_case ("min_age"), or kebab-case ("min-age")
            String[] queryParam = queryParameterMap.get(field.getName());

            if (queryParam == null) {
                queryParam = queryParameterMap.get(StyleCase.toSnakeCase(field.getName()));
            }

            if (queryParam == null) {
                queryParam = queryParameterMap.get(StyleCase.toKebabCase(field.getName()));
            }

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
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
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
        String body = request.getReader() != null
                ? request.getReader().lines().reduce("", (acc, line) -> acc + line + "\n").trim()
                : null;

        // If the body content is not empty, parse it as JSON and store in rootNode
        if (body != null && !body.isEmpty()) {
            return MAPPER.readTree(body);
        } else {
            return null;
        }
    }

    /**
     * Extracts the Authorization header as a Bearer token.
     * 
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
        String setterName = StyleCase.toSetterName(name);

        // try setter methods first
        try {
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equalsIgnoreCase(setterName) || m.getParameterCount() != 1) {
                    continue;
                }

                Class<?> paramType = m.getParameterTypes()[0];
                Object converted = Conversion.convert(value, paramType);
                m.invoke(instance, converted);

                return;
            }
        } catch (Exception _) {
            // No setter found, fallback to field
        }
    }
}
