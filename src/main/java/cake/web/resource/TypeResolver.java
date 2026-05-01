package cake.web.resource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import cake.web.exchange.content.Convertion;

/**
 * Utility class for resolving the appropriate method to call on a resource class based on the HTTP method name and path parameters.
 * It attempts to find a method that matches the given method name and can accept the path parameters by converting them to the 
 * required types. If multiple methods match or if no method matches, it throws a NoSuchMethodException.
 */
public class TypeResolver {
    private TypeResolver() {
        // static class
    }

    /**
     * Finds methods that match the given path parameters based on type compatibility.
     * 
     * @param resourceClass the class containing the methods
     * @param methodName the HTTP method name (get, post, put, delete)
     * @param pathParams the path parameter values from the request
     * @return List of methods that are compatible with the path parameters
     * @throws NoSuchMethodException if no compatible method is found or if the call is ambiguous
     */
    public static MethodResolution methodResolution(Class<?> resourceClass, String methodName, List<String> pathParams) throws NoSuchMethodException {
        if(resourceClass == null || methodName == null || pathParams == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        
        List<Method> filteredMethods = Arrays.stream(resourceClass.getMethods())
            .filter(m -> m.getName().equals(methodName))
            .toList();
        

        List<MethodResolution> candidates = new ArrayList<>();
        List<Object> parameterData = null;

        for(Method m: filteredMethods) {
            parameterData = createParameterDataList(m, pathParams);

            if(!parameterData.isEmpty()) {
                candidates.add(new MethodResolution(m, parameterData));
            }
        }

        if (candidates.isEmpty()) {
            throw new NoSuchMethodException(
                "No method named '" + methodName + "' in " + resourceClass.getName() + 
                " compatible with path parameters: " + pathParams
            );
        }

        if (candidates.size() > 1) {
            throw new NoSuchMethodException(
                "Ambiguous call: multiple methods named '" + methodName + "' in " + 
                resourceClass.getName() + " are compatible with path parameters: " + pathParams
            );
        }

        return candidates.getFirst(); // Return the single compatible method
    }

    /**
     * Converts the given path parameter strings to their corresponding types based on the method's parameter types.
     * @param method the method for which to convert parameters
     * @param pathParams the path parameter values from the request
     * @return List of DataType objects containing the target type and converted value for each parameter
     */
    private static List<Object> createParameterDataList(Method method, List<String> pathParams) {
        if(method == null || pathParams == null) {
            return List.of();
        }

        Class<?>[] parameterTypes = method.getParameterTypes();

        if (pathParams.size() != parameterTypes.length) {
            return List.of();
        }

        try {
            return IntStream.range(0, pathParams.size())
                .mapToObj(n -> Convertion.convert(pathParams.get(n), parameterTypes[n]))
                .toList();
        } catch (Exception _) {
            return List.of();
        }
    }
}
