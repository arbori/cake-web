package cake.web.resource;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import cake.web.exception.AmbiguityException;
import cake.web.exception.PrimitiveNotAllowedException;
import cake.web.exchange.HttpDataHandle;
import cake.web.exchange.HttpMethodName;
import cake.web.exchange.content.BodyContent;
import cake.web.exchange.content.Convertion;
import cake.web.exchange.content.HeaderContent;
import cake.web.exchange.content.QueryParamContent;

/**
 * Utility class for resolving the appropriate method to call on a resource class based on the HTTP method name and path parameters.
 * It attempts to find a method that matches the given method name and can accept the path parameters by converting them to the 
 * required types.
 * Overload endpoint is a limitation of the framework. So, only one http method representation with the same number of parameters is
 * allowed, if multiple methods match or if no method matches, it throws a NoSuchMethodException.
 */
public class MethodResolver {
    private MethodResolver() {
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
    public static Method methodResolution(Class<?> resourceClass, HttpMethodName httpMethodName, List<Object> pathParams) throws NoSuchMethodException, AmbiguityException {
        if(resourceClass == null || httpMethodName == null || pathParams == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
 
        // Private constructor do not allowed create an object of the resource class.
        boolean isConstructorPublic;

        try {
            isConstructorPublic = Modifier.isPublic(resourceClass.getConstructor().getModifiers());
        } catch (NoSuchMethodException _) {
            throw new NoSuchMethodException("Failed to get the constructor of resource class: " + resourceClass.getName() + ".\nEnsure it has a public no-arg constructor.");
        }

        if (!isConstructorPublic) {
            throw new NoSuchMethodException("Resource class: " + resourceClass.getName() + " has no public no-arg constructor.");
        }

        // Search for public and non-static http method in the resource class.
        List<Method> filteredMethods = Arrays.stream(resourceClass.getMethods())
            .filter(m -> (
                m.getName().equals(httpMethodName.toString()) &&
                java.lang.reflect.Modifier.isPublic(m.getModifiers()) &&
                !java.lang.reflect.Modifier.isStatic(m.getModifiers())) &&
                methodParameterMatch(m, pathParams)
            )
            .toList();

        // There is no correspondent http method.
        if(filteredMethods.isEmpty()) { 
            throw new NoSuchMethodException(
                "No public non-static method named " + resourceClass.getName() + "." + httpMethodName + " found."
            );
        }

        // Only one method with the same number of path parameters is allowed. Otherwise, it is ambiguous.
        if (filteredMethods.size() > 1) {
            throw new AmbiguityException(
                "Ambiguity call to " + resourceClass.getName() + "." + httpMethodName +  
                ". Endpoint overload is not allowed.\n" +
                filteredMethods.stream()
                    .map(m -> m.getName() + "(" + formatParamTypes(m.getParameterTypes()) + ")")
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("") 
            );
        }

        return filteredMethods.getFirst();
    }

    /**
     * Check if the method fit with path param and other http data as body, header and query parameters.
     * The method can has a mix of parameter that source is a path parameter, body constent, header attribute 
     * or query parameter. This method check if every source match with method parameter.
     * 
     * @param method the method to check
     * @param pathParams the path parameters
     * @return Return true if maethod parameters match with the sources
     */
    private static boolean methodParameterMatch(Method method, List<Object> pathParams) {
        // If null or empty, the result is an empty list.
        if(method == null || pathParams == null || (method.getParameterCount() < pathParams.size())) {
            return false;
        }

        List<Class<?>> parameterTypes = Arrays.asList(method.getParameterTypes());
        
        // Counters of parameters already found
        int numberOfPathParam = pathParams.size();
        int numberOfBodyContent = 0;
        int numberOfQueryParam = 0;
        int numberOfHeaderContent = 0;
        
        // Count what kind of parameters are already found
        for(var paramIterator = parameterTypes.iterator(); paramIterator.hasNext(); ) {
            Class<?> paramType = paramIterator.next();
            
            // Parameter type should correspond to a path parameter type if:
            //     1. The parameter type is a converseble type for the framework or;
            //     2. There is path parameter(s) and;
            //     3. All path parameters has not been checked and;
            //     4. The types are equals.
            boolean shouldCorrespondType = Convertion.isBasicConversebleType(paramType) ||
                !pathParams.isEmpty() &&
                ((pathParams.size() - numberOfPathParam) < pathParams.size() && 
                paramType.equals(pathParams.get(pathParams.size() - numberOfPathParam).getClass()));

            if(shouldCorrespondType) {
                numberOfPathParam--;
            } else if(BodyContent.class.isAssignableFrom(paramType)) {
                numberOfBodyContent++;
            } else if(QueryParamContent.class.isAssignableFrom(paramType)) {
                numberOfQueryParam++;
            } else if(HeaderContent.class.isAssignableFrom(paramType)) {
                numberOfHeaderContent++;
            }
        }

        // The method will be choose if it has all parameters of path param and one or none of body, header and query data.
        return (numberOfPathParam == 0 && numberOfBodyContent <= 1 && numberOfQueryParam <= 1 && numberOfHeaderContent <= 1);
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
    public static Optional<List<Object>> createParameterDataList(Method method, List<Object> pathParams, HttpDataHandle httpDataHandle) {
        if(method == null || pathParams == null) {
            return Optional.empty();
        }

        Class<?>[] parameterTypes = method.getParameterTypes();

        if (pathParams.size() > parameterTypes.length) {
            return Optional.empty();
        }

        try {
            return Optional.of(convertPathParams(parameterTypes, pathParams, httpDataHandle));
        } catch (Exception _) {
            return Optional.empty();
        }
    }

    /** 
     * Converts a list of path parameter values to the specified target types based on their positions.
     * @param parameterTypes the array of target parameter types corresponding to each path parameter or the parent resource result.
     * @param pathParams the list of path parameter values (as objects)
     * @return a list of converted objects corresponding to each path parameter and/or the parent resource result.
     * @throws IOException if the boby can not be read.
     * @throws ArrayIndexOutOfBoundsException if the number of parameter types is less than the number of path parameters.
     * @throws PrimitiveNotAllowedException if a parameter type is a primitive type.
     */
    protected static List<Object> convertPathParams(Class<?>[] parameterTypes, List<Object> pathParams, HttpDataHandle httpDataHandle) throws IOException {
        if(parameterTypes.length < pathParams.size()) {
            throw new ArrayIndexOutOfBoundsException("The number of path parameters (" + pathParams.size() + ") is bigger than the number of parameter types (" + parameterTypes.length + ").");
        }

        List<Object> result = new ArrayList<>(parameterTypes.length);
        List<Class<?>> interfaces;

        for(int i = 0; i < parameterTypes.length; i++) {
            if(parameterTypes[i].isPrimitive()) {
                throw new PrimitiveNotAllowedException("Parameter with type " + parameterTypes[i].getName() + " is not allowed");
            }

            interfaces = Arrays.asList(parameterTypes[i].getInterfaces());

            if(interfaces.contains(BodyContent.class)) {
                result.add(httpDataHandle.buildFromBody(parameterTypes[i]));
            }
            else if(interfaces.contains(QueryParamContent.class)) {
                result.add(httpDataHandle.buildFromQueryParameter(parameterTypes[i]));
            }
            else if(interfaces.contains(HeaderContent.class)) {
                result.add(httpDataHandle.buildFromHeader(parameterTypes[i]));
            }
            else if(Convertion.isBasicConversebleType(parameterTypes[i])) {
                result.add(Convertion.convert(pathParams.get(i), parameterTypes[i]));
            }
            else if(parameterTypes[i].isAssignableFrom(pathParams.get(i).getClass())) {
                result.add(pathParams.get(i));
            }
        }

        return result;
    }
}
