package cake.web.resource;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import cake.web.exchange.HttpMethodName;
import cake.web.exchange.content.Convertion;

/**
 * Utility class for resolving the appropriate method to call on a resource class based on the HTTP method name and path parameters.
 * It attempts to find a method that matches the given method name and can accept the path parameters by converting them to the 
 * required types.
 * Overload endpoint is a limitation of the framework. So, only one http method representation with the same number of parameters is
 * allowed, if multiple methods match or if no method matches, it throws a NoSuchMethodException.
 */
public class TypeResolver {
    private TypeResolver() {
        // static class
    }

    /**
     * Finds the method in the given resource class that match the given http method
     * name and can accept the given path parameters based on type compatibility. 
     * 
     * @param resourceClass the class that represents the endpoint
     * @param httpMethodName the HTTP method name (get, post, put, delete) want to call
     * @param pathParams the path parameter values from the request
     * @return The resolution of the method to call and its converted arguments
     * @throws NoSuchMethodException if no compatible method is found or if the call is ambiguous
     */
    public static MethodResolution methodResolution(Class<?> resourceClass, HttpMethodName httpMethodName, List<Object> pathParams) throws NoSuchMethodException {
        if(resourceClass == null || httpMethodName == null || pathParams == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        
        // Private constructor do not allowed create an object of the resource class.
        try {
            if (!Modifier.isPublic(resourceClass.getConstructor().getModifiers())) {
                throw new NoSuchMethodException("Resource class: " + resourceClass.getName() + " has no public no-arg constructor.");
            }
        } catch (NoSuchMethodException _) {
            throw new NoSuchMethodException("Failed to get the constructor of resource class: " + resourceClass.getName() + ".\nEnsure it has a public no-arg constructor.");
        }

        // Search for public and non-static http method in the resource class.
        List<Method> filteredMethods = Arrays.stream(resourceClass.getDeclaredMethods())
            .filter(m -> (
                m.getName().equals(httpMethodName.toString()) &&
                java.lang.reflect.Modifier.isPublic(m.getModifiers()) &&
                !java.lang.reflect.Modifier.isStatic(m.getModifiers())) &&
                (m.getParameterCount() == pathParams.size())
            )
            .toList();

        // If the method was no found, it means that the resource do not process this http method.
        if(filteredMethods.isEmpty()) {
            filteredMethods = Arrays.stream(resourceClass.getMethods())
            .filter(m -> (
                m.getName().equals(httpMethodName.toString()) &&
                java.lang.reflect.Modifier.isPublic(m.getModifiers()) &&
                !java.lang.reflect.Modifier.isStatic(m.getModifiers())) &&
                (m.getParameterCount() == pathParams.size())
            )
            .toList();

            if(filteredMethods.isEmpty()) { 
                throw new NoSuchMethodException(
                    "No public non-static method named " + resourceClass.getName() + "." + httpMethodName + " found."
                );
            }
        }

        // Only one method with the same number of path parameters is allowed. Otherwise, it is ambiguous.
        if (filteredMethods.size() > 1) {
            throw new NoSuchMethodException(
                "Ambiguity call to " + resourceClass.getName() + "." + httpMethodName +  
                ". Endpoint overload is not allowed.\n" +
                filteredMethods.stream()
                    .map(m -> m.getName() + "(" + formatParamTypes(m.getParameterTypes()) + ")")
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("") 
            );
        }

        // Try create the parameter list to call the method.
        Optional<List<Object>> parameterData = createParameterDataList(filteredMethods.getFirst(), pathParams);

        // If there is no return, it means that path parameters is not compatible with the parameter's types of the method.
        if (parameterData.isEmpty()) {
            throw new NoSuchMethodException(
                "No method " + resourceClass.getName() + "." + httpMethodName + 
                " compatible with path parameters: " + pathParams
            );
        }

        return new MethodResolution(filteredMethods.getFirst(), parameterData.get());
    }

    // Helper method to format parameter types for error messages
    private static String formatParamTypes(Class<?>[] types) {
        return Arrays.stream(types).map(Class::getSimpleName).reduce((a,b) -> a + "," + b).orElse("");
    }

    /**
     * Attempts to convert the path parameters to the types required by the method's parameters.
     * @param method the method for which to create the parameter data list
     * @param pathParams the original path parameters as objects
     * @return an Optional containing the list of converted parameter values if successful, or empty if conversion fails
     */
    private static Optional<List<Object>> createParameterDataList(Method method, List<Object> pathParams) {
        if(method == null || pathParams == null) {
            return Optional.empty();
        }

        Class<?>[] parameterTypes = method.getParameterTypes();

        if (pathParams.size() != parameterTypes.length) {
            return Optional.empty();
        }

        try {
            return Optional.of(Convertion.convertPathParams(pathParams, parameterTypes));
        } catch (Exception _) {
            return Optional.empty();
        }
    }
}
