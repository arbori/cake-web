package cake.web.resource;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import cake.web.exception.AmbiguityException;
import cake.web.exception.PrimitiveNotAllowedException;
import cake.web.exchange.HttpDataHandle;
import cake.web.exchange.HttpMethodName;
import cake.web.exchange.content.BodyContent;
import cake.web.exchange.content.Conversion;
import cake.web.exchange.content.HeaderContent;
import cake.web.exchange.content.QueryParamContent;

/**
 * <p>
 * Extracts and provides access to HTTP request data for the framework.
 * </p>
 * 
 * <p>
 * This class acts as a unified data source for all HTTP request information:
 * query parameters, headers, body content, and authentication tokens. It also
 * provides factory methods to build strongly-typed objects from these data
 * sources.
 * </p>
 * 
 * <h3>Supported Data Sources</h3>
 * <ul>
 * <li><b>Body:</b> JSON content parsed via Jackson, wrapped with class name
 * key</li>
 * <li><b>Query Parameters:</b> URL query string, mapped to field names</li>
 * <li><b>Headers:</b> HTTP headers, mapped to field names
 * (case-insensitive)</li>
 * <li><b>Authentication:</b> Bearer token from Authorization header</li>
 * </ul>
 * 
 * <h3>JSON Format Requirement</h3>
 * <p>
 * For body content, the JSON must be wrapped in an object with the key matching
 * the class name in lowercase. For example, for {@code CustomerRequest}, the
 * expected
 * JSON is {@code {"customerRequest": {...}}}.
 * </p>
 * 
 * <h3>Field Mapping</h3>
 * <ul>
 * <li><b>Query Parameters:</b> Field names must match query parameter names
 * exactly</li>
 * <li><b>Headers:</b> Field names match header names (case-insensitive).
 * CamelCase field names are also matched to kebab-case (e.g., {@code traceId} →
 * {@code Trace-Id})</li>
 * </ul>
 * 
 * <h3>Design Intention</h3>
 * <p>
 * This class centralizes all request data extraction, making it easier to
 * maintain
 * and extend the framework's data binding capabilities.
 * </p>
 * 
 * <h3>Thread Safety</h3>
 * <p>
 * This class is not thread-safe. A new instance is created per request.
 * </p>
 * 
 * @since 1.0.0
 * @see #buildFromBody(Class)
 * @see #buildFromHeader(Class)
 * @see #buildFromQueryParameter(Class)
 */
public class MethodResolver {
    private MethodResolver() {
        // static class
    }

    /**
     * Finds the method in the given resource class that match the given http method
     * name and can accept the given path parameters based on type compatibility.
     * 
     * @param resourceClass        the class that represents the endpoint
     * @param httpMethodName       the HTTP method name (get, post, put, delete)
     *                             want to call
     * @param pathParams           the path parameter values from the request
     * @param isBodyContentAnArray identify if body content is an array.
     * @return The resolution of the method to call and its converted arguments
     * @throws NoSuchMethodException if no compatible method is found or if the call
     *                               is ambiguous
     */
    public static Method methodResolution(Class<?> resourceClass, HttpMethodName httpMethodName,
            List<Object> pathParams, boolean isBodyContentAnArray) throws NoSuchMethodException, AmbiguityException {
        if (resourceClass == null || httpMethodName == null || pathParams == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }

        // Private constructor do not allowed create an object of the resource class.
        boolean isConstructorPublic;

        try {
            isConstructorPublic = Modifier.isPublic(resourceClass.getConstructor().getModifiers());
        } catch (NoSuchMethodException _) {
            throw new NoSuchMethodException("Failed to get the constructor of resource class: "
                    + resourceClass.getName() + ".\nEnsure it has a public no-arg constructor.");
        }

        if (!isConstructorPublic) {
            throw new NoSuchMethodException(
                    "Resource class: " + resourceClass.getName() + " has no public no-arg constructor.");
        }

        // Search for public and non-static http method in the resource class.
        List<Method> filteredMethods = Arrays.stream(resourceClass.getMethods())
                .filter(m -> (m.getName().equals(httpMethodName.toString()) &&
                        Modifier.isPublic(m.getModifiers()) &&
                        !Modifier.isStatic(m.getModifiers())) &&
                        methodParameterMatch(m, pathParams, isBodyContentAnArray))
                .toList();

        // There is no correspondent http method.
        if (filteredMethods.isEmpty()) {
            throw new NoSuchMethodException(
                    "No public non-static method named " + resourceClass.getName() + "." + httpMethodName + " found.");
        }

        // Only one method with the same number of path parameters is allowed.
        // Otherwise, it is ambiguous.
        if (filteredMethods.size() > 1) {
            throw new AmbiguityException(
                    "Ambiguity call to " + resourceClass.getName() + "." + httpMethodName +
                            ". Endpoint overload is not allowed.\n" +
                            filteredMethods.stream()
                                    .map(m -> m.getName() + "(" + formatParamTypes(m.getParameterTypes()) + ")")
                                    .reduce((a, b) -> a + "\n" + b)
                                    .orElse(""));
        }

        return filteredMethods.getFirst();
    }

    /**
     * Check if the method fit with path param and other http data as body, header
     * and query parameters.
     * The method can has a mix of parameter that source is a path parameter, body
     * constent, header attribute
     * or query parameter. This method check if every source match with method
     * parameter.
     * 
     * @param method     the method to check
     * @param pathParams the path parameters
     * @return Return true if maethod parameters match with the sources
     */
    private static boolean methodParameterMatch(Method method, List<Object> pathParams, boolean isBodyContentAnArray) {
        // If null or empty, the result is an empty list.
        if (method == null || pathParams == null || (method.getParameterCount() < pathParams.size())) {
            return false;
        }

        Class<?>[] paramTypes = method.getParameterTypes();
        int numberOfParamTypes = paramTypes.length;

        // Counters of parameters already found
        int numberOfPathParam = pathParams.size();
        int numberOfBodyContent = 0;
        int numberOfQueryParam = 0;
        int numberOfHeaderContent = 0;

        // Count what kind of parameters are already found
        for (int p = 0; p < paramTypes.length; p++) {
            // Parameter type should correspond to a path parameter type if:
            // 1. The parameter type is a converseble type for the framework or;
            // 2. There is path parameter(s) and;
            // 3. All path parameters has not been checked and;
            // 4. The types are equals.
            boolean shouldCorrespondType = Conversion.isBasicConversebleType(paramTypes[p]) ||
                    !pathParams.isEmpty() &&
                            ((pathParams.size() - numberOfPathParam) < pathParams.size() &&
                                    paramTypes[p]
                                            .equals(pathParams.get(pathParams.size() - numberOfPathParam).getClass()));

            if (shouldCorrespondType) {
                numberOfPathParam--;
                numberOfParamTypes--;
            } else if (BodyContent.class.isAssignableFrom(paramTypes[p]) && !isBodyContentAnArray) {
                numberOfBodyContent++;
                numberOfParamTypes--;
            } else if (QueryParamContent.class.isAssignableFrom(paramTypes[p])) {
                numberOfQueryParam++;
                numberOfParamTypes--;
            } else if (HeaderContent.class.isAssignableFrom(paramTypes[p])) {
                numberOfHeaderContent++;
                numberOfParamTypes--;
            }
            // If the type of method parameter is an array or list and the body content is
            // an array,
            // assum that the content can be converted to the componente type class.
            else if (isBodyContentAnArray
                    && (paramTypes[p].isArray() || paramTypes[p].getName().equals(List.class.getName()))) {
                numberOfParamTypes--;
            }
        }

        // The method will be choose if each its parameter was associated to at last one
        // path param or
        // to a body contente, or to a query parameter or a header content.
        return (numberOfParamTypes == 0 && numberOfPathParam == 0 && numberOfBodyContent <= 1 && numberOfQueryParam <= 1
                && numberOfHeaderContent <= 1);
    }

    // Helper method to format parameter types for error messages
    private static String formatParamTypes(Class<?>[] types) {
        return Arrays.stream(types).map(Class::getSimpleName).reduce((a, b) -> a + "," + b).orElse("");
    }

    /**
     * Attempts to convert the path parameters to the types required by the method's
     * parameters.
     * 
     * @param method     the method for which to create the parameter data list
     * @param pathParams the original path parameters as objects
     * @return an Optional containing the list of converted parameter values if
     *         successful, or empty if conversion fails
     */
    public static Optional<List<Object>> createParameterDataList(Method method, List<Object> pathParams,
            HttpDataHandle httpDataHandle) {
        if (method == null || pathParams == null) {
            return Optional.empty();
        }

        if (pathParams.size() > method.getParameterCount()) {
            return Optional.empty();
        }

        try {
            return Optional.of(convertPathParams(method, pathParams, httpDataHandle));
        } catch (Exception _) {
            return Optional.empty();
        }
    }

    /**
     * Converts a list of path parameter values to the specified target types based
     * on their positions.
     * 
     * @param method         the method to convert parameters for
     * @param pathParams     the list of path parameter values (as objects)
     * @param httpDataHandle the HTTP data handle
     * @return a list of converted objects corresponding to each parameter.
     * @throws IOException                    if the body cannot be read.
     * @throws ArrayIndexOutOfBoundsException if the number of parameter types is
     *                                        less than the number of path
     *                                        parameters.
     * @throws PrimitiveNotAllowedException   if a parameter type is a primitive
     *                                        type.
     */
    protected static List<Object> convertPathParams(Method method, List<Object> pathParams,
            HttpDataHandle httpDataHandle) throws IOException {
        var parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();

        if (parameterTypes.length < pathParams.size()) {
            throw new ArrayIndexOutOfBoundsException("The number of path parameters (" + pathParams.size()
                    + ") is bigger than the number of parameter types (" + parameterTypes.length + ").");
        }

        List<Object> result = new ArrayList<>(parameterTypes.length);
        List<Class<?>> interfaces;
        int pathParamIndex = 0;

        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].isPrimitive()) {
                throw new PrimitiveNotAllowedException(
                        "Parameter with type " + parameterTypes[i].getName() + " is not allowed");
            }

            interfaces = Arrays.asList(parameterTypes[i].getInterfaces());

            if (interfaces.contains(BodyContent.class)) {
                result.add(httpDataHandle.buildFromBody(parameterTypes[i]));
            } else if (interfaces.contains(QueryParamContent.class)) {
                result.add(httpDataHandle.buildFromQueryParameter(parameterTypes[i]));
            } else if (interfaces.contains(HeaderContent.class)) {
                result.add(httpDataHandle.buildFromHeader(parameterTypes[i]));
            } else if (Conversion.isBasicConversebleType(parameterTypes[i])) {
                result.add(Conversion.convert(pathParams.get(pathParamIndex++), parameterTypes[i]));
            } else if (parameterTypes[i].isArray()) {
                result.add(httpDataHandle.buildArrayFromBody(parameterTypes[i]));
            } else if (parameterTypes[i].getName().equals(List.class.getName())) {
                result.add(httpDataHandle.buildListFromBody(genericParameterTypes[i]));
            } else if (pathParamIndex < pathParams.size() && parameterTypes[i].isAssignableFrom(pathParams.get(pathParamIndex).getClass())) {
                result.add(pathParams.get(pathParamIndex++));
            }
        }

        return result;
    }
}
