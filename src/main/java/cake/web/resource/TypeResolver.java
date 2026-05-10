package cake.web.resource;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import cake.web.exchange.HttpMethodName;
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
     * @param httpMethodName the HTTP method name (get, post, put, delete)
     * @param pathParams the path parameter values from the request
     * @return List of methods that are compatible with the path parameters
     * @throws NoSuchMethodException if no compatible method is found or if the call is ambiguous
     */
    public static MethodResolution methodResolution(Class<?> resourceClass, HttpMethodName httpMethodName, List<Object> pathParams) throws NoSuchMethodException {
        if(resourceClass == null || httpMethodName == null || pathParams == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        
        try {
            if (!Modifier.isPublic(resourceClass.getConstructor().getModifiers())) {
                throw new NoSuchMethodException("Resource class: " + resourceClass.getName() + " has no public no-arg constructor.");
            }
        } catch (NoSuchMethodException _) {
            throw new NoSuchMethodException("Failed to get the constructor of resource class: " + resourceClass.getName() + ".\nEnsure it has a public no-arg constructor.");
        }

        List<Method> filteredMethods = Arrays.stream(resourceClass.getMethods())
            .filter(m -> (
                m.getName().equals(httpMethodName.toString()) &&
                java.lang.reflect.Modifier.isPublic(m.getModifiers()) &&
                !java.lang.reflect.Modifier.isStatic(m.getModifiers())))
            .toList();

        if(filteredMethods.isEmpty()) {
            throw new NoSuchMethodException(
                "No public non-static method named '" + httpMethodName + "' found in " + resourceClass.getName()
            );
        }

        List<MethodResolution> candidates = new ArrayList<>();
        Optional<List<Object>> parameterData;

        for(Method m: filteredMethods) {
            parameterData = createParameterDataList(m, pathParams);

            if(parameterData.isPresent()) {
                candidates.add(new MethodResolution(m, parameterData.get()));
            }
        }

        if (candidates.isEmpty()) {
            throw new NoSuchMethodException(
                "No method named '" + httpMethodName + "' in " + resourceClass.getName() + 
                " compatible with path parameters: " + pathParams
            );
        }

        if (candidates.size() > 1) {
            throw new NoSuchMethodException(
                "Ambiguous call: multiple methods named '" + httpMethodName + "' in " + 
                resourceClass.getName() + " are compatible with path parameters: " + pathParams +
                "\nCandidates: " + candidates.stream()
                    .map(c -> c.method().getName() + "(" + formatParamTypes(c.method().getParameterTypes()) + ")")
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("") 
            );
        }

        return candidates.getFirst(); // Return the single compatible method
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
