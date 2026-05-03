package cake.web.resource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
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
    public MethodResolution findHttpMethod(Class<?> resourceClass, List<String> pathParams, HttpMethodName httpMethodName) 
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
        int paramCount = pathParams.size();
        String cacheKey = buildCacheKey(resourceClass, methodName, paramCount);
                
        // First, find the unique method by name and parameter count
        Method resolvedMethod = methodCache.get(cacheKey);

        MethodResolution resolution = null;

        if (resolvedMethod == null) {        
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
    private String buildCacheKey(Class<?> resourceClass, String httpMethodName, int paramCount) {
        return new StringBuilder(resourceClass.getName())
            .append("#")
            .append(httpMethodName)
            .append("#")
            .append(paramCount)
            .toString();
    }
    
    /**
     * Finds a method on the resource class by exact name and parameter count.
     * Throws an exception if zero or multiple methods are found.
     * 
     * @param resourceClass the class to inspect
     * @param methodName the method name (case-sensitive, expected in lowercase)
     * @param paramCount the exact number of parameters
     * @return the unique matching method
     * @throws NoSuchMethodException if no method found or multiple ambiguous methods found
     */
    private Method findMethodByExactParamCount(Class<?> resourceClass, String methodName, int paramCount) 
            throws NoSuchMethodException {

        if(resourceClass == null) {
            throw new NoSuchMethodException("Resource class cannot be null");
        }
        if(methodName == null) {
            throw new NoSuchMethodException("Method name cannot be null");
        }
        if(paramCount < 0) {
            throw new NoSuchMethodException("Parameter count cannot be negative");
        }

        try {
            Constructor<?> ctor = resourceClass.getConstructor();  // ✅ Only checks, no instantiation
            if (!Modifier.isPublic(ctor.getModifiers())) {
                throw new NoSuchMethodException("Resource class: " + resourceClass.getName() + " has no public no-arg constructor.");
            }
        } catch (NoSuchMethodException _) {
            throw new NoSuchMethodException("Failed to get the constructor of resource class: " + resourceClass.getName() + ".\nEnsure it has a public no-arg constructor.");
        }

        List<Method> matchingMethods = Arrays.stream(resourceClass.getMethods())
            .filter(m -> (
                java.lang.reflect.Modifier.isPublic(m.getModifiers()) &&
                !java.lang.reflect.Modifier.isStatic(m.getModifiers()) &&
                m.getName().equals(methodName) &&
                m.getParameterCount() == paramCount
            )).toList();
        
        if (matchingMethods.isEmpty()) {
            throw new NoSuchMethodException(
                "No method named '" + methodName + "' with " + paramCount + " parameter(s) in class " + resourceClass.getName() +
                ".\n Ensure the method is public, non-static, and has the correct number of parameters."
            );
        }
        
        if (matchingMethods.size() > 1) {
            // Build detailed ambiguity message
            StringBuilder details = new StringBuilder();
            for (Method m : matchingMethods) {
                if (!details.isEmpty()) details.append(", ");
                details.append(m.getName())
                       .append("(")
                       .append(formatParameterTypes(m.getParameterTypes()))
                       .append(")");
            }
            
            throw new NoSuchMethodException(
                "Ambiguous methods: multiple methods named '" + methodName + 
                "' with " + paramCount + " parameter(s) in class " + resourceClass.getName() + 
                ": " + details.toString()
            );
        }
        
        return matchingMethods.get(0);
    }

    /**
     * Formats parameter types for readable error messages (simple names only).
     */
    private String formatParameterTypes(Class<?>[] paramTypes) {
        return String.join(",", Arrays.stream(paramTypes)
            .map(Class::getSimpleName)
            .toArray(String[]::new));
    }
    
    /**
     * Optional: Clear entire cache.
     */
    public static void clearCache() {
        methodCache.clear();
    }
    
    /**
     * Optional: Clear cache for a specific resource class.
     */
    public static void clearCacheForClass(Class<?> resourceClass) {
        String prefix = resourceClass.getName() + "#";
        methodCache.keySet().removeIf(key -> key.startsWith(prefix));
    }
    
    /**
     * Optional: Get cache size for monitoring.
     */
    public static int getCacheSize() {
        return methodCache.size();
    }
}
