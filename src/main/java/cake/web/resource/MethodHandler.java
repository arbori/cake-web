package cake.web.resource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cake.web.exchange.HttpMethodName;
import cake.web.exchange.content.Convertion;

/**
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
     * @return a MethodResolution object containing the resolved method and converted arguments
     * @throws NoSuchMethodException if no method is found, or if multiple methods match by name and parameter count
     * @throws IllegalArgumentException if path parameters cannot be converted to the required types
     */
    public static MethodResolution findHttpMethod(Class<?> resourceClass, HttpMethodName httpMethodName, List<Object> pathParams) 
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
        Method methodFromCache = methodCache.get(cacheKey);

        MethodResolution methodResolution = null;

        if(methodFromCache != null) {
            List<Object> convertedArgs = Convertion.convertPathParams(pathParams, methodFromCache.getParameterTypes());

            methodResolution = new MethodResolution(methodFromCache, convertedArgs);
        } else {        
            methodResolution = TypeResolver.methodResolution(resourceClass, httpMethodName, pathParams); // Validate method resolution first (throws if no method or ambiguous)

            methodCache.put(cacheKey, methodResolution.method());
        } 
        
        return methodResolution;
    }

    /**
     * Builds a cache key based on the resource class name, HTTP method name, and parameter count.
     * @param resourceClass the class of the resource
     * @param httpMethodName the HTTP method name (e.g., GET, POST)
     * @param paramCount the number of parameters
     * @return a unique cache key string
     */
    private static String buildCacheKey(Class<?> resourceClass, String httpMethodName, List<Object> pathParams) {
        StringBuilder sb = new StringBuilder();

        if(!pathParams.isEmpty()) {
            pathParams.forEach(param -> 
                sb.append((param instanceof String) ? 
                        Convertion.kindOfParamType(param) : 
                        param.getClass().getName())
                    .append(","));
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
