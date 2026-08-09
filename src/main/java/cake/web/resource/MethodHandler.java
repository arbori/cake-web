package cake.web.resource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import cake.web.exception.AmbiguityException;
import cake.web.exchange.HttpDataHandle;
import cake.web.exchange.HttpMethodName;
import cake.web.exchange.content.Convertion;

/**
 * <p>Handles method resolution with caching for performance optimization.</p>
 * 
 * <p>This class acts as the facade for method resolution in Cake Web. It wraps the
 * {@link MethodResolver} logic and adds a caching layer to avoid repeated reflection
 * for the same method signatures.</p>
 * 
 * <h3>Caching Strategy</h3>
 * <ul>
 *   <li><b>Cache Key:</b> {@code <className>#<parameterTypeHints>#<httpMethodName>}</li>
 *   <li><b>Parameter Type Hints:</b> For String values, the framework uses {@code kindOfParamType()}
 *       to infer the type; for other objects, the class name is used</li>
 *   <li><b>Cache Validation:</b> On cache hit, the framework verifies that conversion still works
 *       with the current path parameters</li>
 * </ul>
 * 
 * <h3>Design Intention</h3>
 * <p>Method resolution via reflection is relatively expensive. This class reduces the cost
 * to O(1) after the first request for a given method signature. The cache key uses
 * type hints rather than full parameter types to handle cases where the actual values
 * differ but the type signature is the same (e.g., "123" and "456" are both integers).</p>
 * 
 * <h3>Thread Safety</h3>
 * <p>This class uses {@link ConcurrentHashMap} for thread-safe caching.</p>
 * 
 * @since 1.0.0
 * @see #findHttpMethod(Class, HttpMethodName, List, HttpDataHandle)
 * @see MethodResolver
 */
public class MethodHandler {
    private MethodHandler() {
        // static class
    }
    
    // Cache to store resolved methods based on resource class, HTTP method name, and parameter types
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    /**
     * Finds the appropriate method on the resource class to handle the HTTP request.
     * Resolution is based ONLY on method name and parameter count.
     * 
     * @param resourceClass the class of the resource
     * @param pathParams the list of path parameter values from the URL (used only for count and conversion)
     * @param httpMethodName the HTTP method name (e.g., GET, POST)
     * @param httpMetadataHandle 
     * @param pathParams the list of path parameter values from the URL (used only for count and conversion)
     * @return a MethodResolution object containing the resolved method and converted arguments
     * @throws NoSuchMethodException if no method is found, or if multiple methods match by name and parameter count
     * @throws IllegalArgumentException if path parameters cannot be converted to the required types
     * @throws AmbiguityException if there is ambiguity calling the method
     */
    public static MethodResolution findHttpMethod(Class<?> resourceClass, HttpMethodName httpMethodName, List<Object> pathParams, HttpDataHandle httpDataHandle) 
            throws NoSuchMethodException, IllegalArgumentException, AmbiguityException 
    {
        if(resourceClass == null) {
            throw new IllegalArgumentException("Resource class cannot be null");
        }
        if(httpMethodName == null) {
            throw new IllegalArgumentException("HTTP method name cannot be null");
        }
        if(pathParams == null) {
            throw new NoSuchMethodException("Path parameters list cannot be null");
        }        
        
        String methodName = httpMethodName.toString().toLowerCase();
        String cacheKey = buildCacheKey(resourceClass, methodName, pathParams);
                
        Optional<List<Object>> convertedArgsOptional;

        // Look for the method in the cache.
        Method methodFromCache = methodCache.get(cacheKey);

        // If method is is the cache, it will be used ...
        if(methodFromCache != null) {
            // ... to convert path parameters to the required method's parameters types.
            convertedArgsOptional = MethodResolver.createParameterDataList(methodFromCache, pathParams, httpDataHandle);

            // If the conversion fail, path parameters is not compatible with the parameter's types of the method.
            if(convertedArgsOptional.isEmpty()) {
                throw new IllegalArgumentException("Path parameters cannot be converted to the required parameters types of cached method.");
            }

            // Every thing is ok, return the method and converted arguments.
            return new MethodResolution(methodFromCache, convertedArgsOptional.get());
        }

        // So. The method is not in the cache. Then, resuolve the proper method to call.
        Method method = MethodResolver.methodResolution(resourceClass, httpMethodName, pathParams);

        // Try to convert the path parameters to the required method's parameters types.
        convertedArgsOptional = MethodResolver.createParameterDataList(method, pathParams, httpDataHandle);

        // If convertion fails, there is no compatibility between path parameters and method parameters.
        if(convertedArgsOptional.isEmpty()) {
            throw new IllegalArgumentException("Path parameters cannot be converted to the required method parameters types");
        }

        // The method was found. Put it in the cache.
        methodCache.put(cacheKey, method);

        // Everything is ok, return the resolved method.
        return new MethodResolution(method, convertedArgsOptional.get());
    }

    /**
     * Builds a cache key based on the resource class name, HTTP method name, and parameter count.
     * @param resourceClass the class of the resource
     * @param httpMethodName the HTTP method name (e.g., GET, POST)
     * @param paramCount the number of parameters
     * @return a unique cache key string
     */
    public static String buildCacheKey(Class<?> resourceClass, String httpMethodName, List<Object> pathParams) {
        StringBuilder sb = new StringBuilder();

        if(!pathParams.isEmpty()) {
            pathParams.forEach(param -> 
                sb.append((param instanceof String) ? 
                        Convertion.kindOfParamType(param) : 
                        param.getClass().getName())
                    .append(", "));

            sb.setLength(sb.length() - 2); // Remove trailing comma
        }

        return new StringBuilder(resourceClass.getName())
            .append("#")
            .append(sb.toString())
            .append("#")
            .append(httpMethodName)
            .toString();
    }
}
