package cake.web.resource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cake.web.exchange.HttpMethodName;
import cake.web.exchange.content.Convertion;

/**
 * A utility class responsible for resolving the appropriate method on a resource
 * class to handle an HTTP request based on the HTTP method name and parameter count only.
 * 
 * If multiple methods exist with the same name and same parameter count, ambiguity is
 * detected and an exception is thrown — no type inference or scoring is performed.
 * 
 * The cache key includes the full parameter type names, ensuring that if a resource class
 * changes its method signatures, stale cache entries are not reused.
 */
public class MethodHandler {
    // Cache to store resolved methods based on resource class, HTTP method name, and parameter types
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    /**
     * Finds the appropriate method on the resource class to handle the HTTP request.
     * Resolution is based ONLY on method name and parameter count.
     * 
     * @param resourceClass the class of the resource
     * @param pathParams the list of path parameter values from the URL (used only for count and conversion)
     * @param httpMethodName the HTTP method name (e.g., GET, POST)
     * @return a MethodResolution object containing the resolved method and converted arguments
     * @throws NoSuchMethodException if no method is found, or if multiple methods match by name and parameter count
     * @throws IllegalArgumentException if path parameters cannot be converted to the required types
     */
    public MethodResolution findHttpMethod(Class<?> resourceClass, HttpMethodName httpMethodName, List<String> pathParams) 
            throws NoSuchMethodException, IllegalArgumentException 
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
                
        // First, find the unique method by name and parameter count
        Method resolvedMethod = methodCache.get(cacheKey);

        MethodResolution resolution = null;

        if(resolvedMethod != null) {
            List<Object> convertedArgs = Convertion.convertPathParams(pathParams, resolvedMethod.getParameterTypes());

            resolution = new MethodResolution(resolvedMethod, convertedArgs);
        } else {        
            resolution = TypeResolver.methodResolution(resourceClass, httpMethodName, pathParams); // Validate method resolution first (throws if no method or ambiguous)

            methodCache.put(cacheKey, resolution.method());
        } 
        
        return resolution;
    }

    /**
     * Builds a cache key based on the resource class name, HTTP method name, and parameter count.
     * @param resourceClass the class of the resource
     * @param httpMethodName the HTTP method name (e.g., GET, POST)
     * @param paramCount the number of parameters
     * @return a unique cache key string
     */
    private String buildCacheKey(Class<?> resourceClass, String httpMethodName, List<String> pathParams) {
        StringBuilder sb = new StringBuilder();

        if(!pathParams.isEmpty()) {
            pathParams.forEach(param -> sb.append(Convertion.kindOfParamType(param)).append(","));
            sb.setLength(sb.length() - 1); // Remove trailing comma
        }

        return new StringBuilder(resourceClass.getName())
            .append("#")
            .append(httpMethodName)
            .append("(")
            .append(sb.toString())
            .append(")")
            .toString();
    }
}
