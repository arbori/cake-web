package cake.web.exchange;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class GetRequestExchange {
    private final String requestURI;
    private final List<String> tokens;

    public GetRequestExchange(String requestURI, String contextPath) {
        if (requestURI == null || requestURI.isEmpty() || contextPath == null || contextPath.isEmpty()) {
            throw new IllegalArgumentException("requestURI and contextPath must be provided.");
        }

        this.requestURI = requestURI;
        this.tokens = tokenizePath(requestURI, contextPath);
    }

    /**
     * Public entry point used by RootServlet.
     *
     * @param queryParameters the request.getParameterMap()
     * @return object returned by terminal resource get(...)
     * @throws NoSuchMethodException when a suitable get(...) is not found
     */
    public Object get(Map<String, String[]> queryParameters) throws NoSuchMethodException {
        // Try different splits for base package (supporting com/bank/loan/customer and loan/client/...)
        for (int baseEnd = 0; baseEnd <= tokens.size() - 1; baseEnd++) {
            String basePackage = join(tokens.subList(0, baseEnd + 1), ".");
            int nextIndex = baseEnd + 1;

            try {
                Optional<Object> result = resolveChainWithBase(basePackage, nextIndex, queryParameters);
            
                if (result.isPresent()) {
                    return result.get();
                }
            } catch (ClassNotFoundException e) {
                // try next base split
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                // These indicate this base-package split isn't viable; try next.
            }
        }

        throw new NoSuchMethodException("No resource chain resolved for URI: " + requestURI);
    }

    /* --------------------- Core chain resolver --------------------- */

    /**
     * Resolves the resource chain starting from the given base package.
     *
     * @param basePackage     the base package to start from
     * @param startIndex      the index in tokens to start processing resources
     * @param queryParameters the request.getParameterMap()
     * @return Optional containing the terminal resource result, or empty if not resolved
     * @throws ClassNotFoundException    if a resource class is not found
     * @throws NoSuchMethodException     if a suitable get(...) method is not found
     * @throws InvocationTargetException if a get(...) method throws an exception
     * @throws IllegalAccessException    if a method or constructor is inaccessible
     * @throws InstantiationException    if a resource class cannot be instantiated
     * @throws IllegalArgumentException  if parameter conversion fails
     */
    private Optional<Object> resolveChainWithBase(String basePackage, int startIndex, Map<String, String[]> queryParameters)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, IllegalArgumentException {
        if (startIndex >= tokens.size()) {
            // nothing after base package -> nothing to handle
            throw new ClassNotFoundException("No resource token after base package: " + basePackage);
        }

        Object previousResult = null;
        int i = startIndex;

        while (i < tokens.size()) {
            String resourceToken = tokens.get(i);
            String candidateClassName = basePackage + "." + capitalize(resourceToken);

            Class<?> resourceClass = tryLoadClass(candidateClassName)
                    .orElseThrow(() -> new ClassNotFoundException("Resource class not found: " + candidateClassName));

            // collect path params destined for this resource until next token that looks like a class
            List<String> pathParams = new ArrayList<>();
            int j = i + 1;
            while (j < tokens.size()) {
                String nextToken = tokens.get(j);
                String nextCandidate = basePackage + "." + capitalize(nextToken);
                if (classExists(nextCandidate)) {
                    break; // next token is another resource class
                }
                pathParams.add(nextToken);
                j++;
            }

            // instantiate resource
            Object resourceInstance = resourceClass.getDeclaredConstructor().newInstance();

            // inject previous result (parent) into this resource instance if present
            if (previousResult != null) {
                injectParent(resourceInstance, previousResult);
            }

            // set query params on resource instance BEFORE calling get(...)
            setAttributes(resourceInstance, queryParameters);

            // find get method suitable for this set of pathParams
            Method getMethod = findGetMethod(resourceClass, pathParams);
            if (getMethod == null) {
                throw new NoSuchMethodException("No suitable get(...) for class " + resourceClass.getName()
                        + " with " + pathParams.size() + " positional parameters.");
            }

            // call the get method
            Object callResult = callGetMethod(resourceInstance, getMethod, pathParams);

            // if not terminal (there are more resource tokens), the callResult becomes previousResult for next resource
            previousResult = callResult;

            // advance to next resource token
            i = j;
        }

        // previousResult now holds terminal result
        return Optional.ofNullable(previousResult);
    }

    /* --------------------- Helpers --------------------- */

    /**
     * Tokenizes the path after the contextPath, splitting on '/' and ignoring empty tokens.
     * @param uri the full request URI
     * @param contextPath the context path to strip
     * @return list of path tokens
     */
    private List<String> tokenizePath(String uri, String contextPath) {
        String path = uri.startsWith(contextPath) 
            ? uri.substring(contextPath.length()) 
            : uri;

        return Arrays.stream(path.split("/"))
            .filter(s -> !s.isEmpty())
            .toList();
    }

    /**
     * Attempts to load a class by fully qualified class name.
     * @param fqcn the fully qualified class name
     * @return Optional containing the Class if found, or empty if not found
     */
    private Optional<Class<?>> tryLoadClass(String fqcn) {
        try {
            return Optional.of(Class.forName(fqcn));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * Checks if a class with the given fully qualified class name exists.
     * @param fqcn the fully qualified class name
     * @return true if the class exists, false otherwise
     */
    private boolean classExists(String fqcn) {
        return tryLoadClass(fqcn).isPresent();
    }

    /**
     * Joins a list of strings with the given separator.
     * @param parts the list of strings
     * @param sep the separator
     * @return the joined string
     */
    private static String join(List<String> parts, String sep) {
        return parts.stream().collect(Collectors.joining(sep));
    }

    /**
     * Capitalizes the first letter of the string.
     * @param s the input string
     * @return the capitalized string
     */
    private static String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Sets attributes on the given instance using the provided parameters map.
     * It tries to find setter methods first, then falls back to direct field access.
     * Only the first value of each parameter is used.
     * @param instance the object instance to set attributes on
     * @param params the map of parameter names to values
     */
    private void setAttributes(Object instance, Map<String, String[]> params) {
        if (params == null || params.isEmpty()) {
            return;
        }

        Class<?> clazz = instance.getClass();

        params.forEach((name, values) -> {
            String value;
            
            if((value = (values != null && values.length > 0) ? values[0] : null) != null) {
                trySetAttributes(name, value, clazz, instance);
            }
        });
    }

    /**
     * Tries to set a single attribute on the given instance by name and value.
     * It first attempts to find a setter method, then falls back to direct field access.
     * @param name the attribute name
     * @param value the attribute value as string
     * @param clazz the class of the instance
     * @param instance the object instance to set the attribute on
     */
    private void trySetAttributes(String name, String value, Class<?> clazz, Object instance) {
        String setterName = "set" + capitalize(name);

        // try setter methods first
        try {
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equalsIgnoreCase(setterName) || m.getParameterCount() != 1)
                    continue;
                Class<?> paramType = m.getParameterTypes()[0];
                Object converted = convert(value, paramType);
                m.invoke(instance, converted);
                return;
            }
        } catch (Exception _) {
            // No setter found, fallback to field
        }

        // Fallback: Direct field access via VarHandle (no accessibility warnings)
        try {
            Field f = clazz.getDeclaredField(name);
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
            VarHandle handle = lookup.unreflectVarHandle(f);
            Object converted = convert(value, f.getType());
            handle.set(instance, converted);
        } catch (Exception _) {
            // There is no consequence to skipping a query parameter
        }
    }

    /**
     * Finds a get method in the given class that matches the number and types of the provided path parameters.
     * @param resourceClass the class to search for the get method
     * @param pathParams the list of path parameters as strings
     * @return the matching Method if found, or null if no suitable method exists
     */
    private static Method findGetMethod(Class<?> resourceClass, List<String> pathParams) {
        return Arrays.stream(resourceClass.getMethods())
            .filter(m -> m.getName().equals("get"))
            .filter(m -> m.getParameterCount() == pathParams.size())
            .filter(m -> {
                Class<?>[] paramTypes = m.getParameterTypes();

                for (int i = 0; i < paramTypes.length; i++) {
                    try {
                        convert(pathParams.get(i), paramTypes[i]);
                    } catch (Exception e) {
                        return false;
                    }
                }

                return true;
            })
            .findFirst()
            .orElse(null);
    }

    /**
     * Calls the given get method on the controller instance with the provided path parameters.
     * @param controllerInstance the instance of the controller
     * @param method the get method to invoke
     * @param pathParams the list of path parameters as strings
     * @return the result of the method invocation
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws IllegalAccessException if this Method object is enforcing Java language access control and the underlying method is inaccessible
     */
    private static Object callGetMethod(Object controllerInstance, Method method, List<String> pathParams)
            throws InvocationTargetException, IllegalAccessException {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = convert(pathParams.get(i), paramTypes[i]);
        }
        return method.invoke(controllerInstance, args);
    }

    /**
     * Attempts to inject parentResult into childInstance:
     * - Prefer setter whose parameter type is assignable from parentResult.getClass()
     *   and whose name matches set<ParentName> (matching stripped suffixes).
     * - Otherwise try any setter with param type assignable from parent.
     * - Otherwise try direct field injection by type + name heuristics.
     */
    private static void injectParent(Object childInstance, Object parentResult) {
        if (parentResult == null) {
            return;
        }

        Class<?> childClass = childInstance.getClass();
        Class<?> parentClass = parentResult.getClass();

        // build candidate setter names: try parent simple name and stripped variants
        String parentSimple = parentClass.getSimpleName();
        String stripped = stripCommonSuffixes(parentSimple);

        List<String> candidateSetterNames = new ArrayList<>();
        candidateSetterNames.add("set" + parentSimple);
        candidateSetterNames.add("set" + stripped);

        // first pass: exact name + assignable type
        for (String setterName : candidateSetterNames) {
            try {
                for (Method m : childClass.getMethods()) {
                    if (!m.getName().equalsIgnoreCase(setterName) || m.getParameterCount() != 1) {
                        continue;
                    }

                    Class<?> paramType = m.getParameterTypes()[0];
                    
                    if (paramType.isAssignableFrom(parentClass)) {
                        m.invoke(childInstance, parentResult);
                        return;
                    }
                }
            } catch (ReflectiveOperationException _) {
            }
        }

        // second pass: any setter with parameter assignable from parent type
        try {
            for (Method m : childClass.getMethods()) {
                if (!m.getName().startsWith("set") || m.getParameterCount() != 1) {
                    continue;
                }

                Class<?> paramType = m.getParameterTypes()[0];
                
                if (paramType.isAssignableFrom(parentClass)) {
                    m.invoke(childInstance, parentResult);
                    return;
                }
            }
        } catch (ReflectiveOperationException _) {
        }

        // third pass: try fields
        for (Field f : childClass.getDeclaredFields()) {
            if (f.getType().isAssignableFrom(parentClass)) {
                try {
                    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(childClass, MethodHandles.lookup());
                    VarHandle handle = lookup.unreflectVarHandle(f);
                    handle.set(childInstance, parentResult);

                    return;
                } catch (ReflectiveOperationException ignored) {}
            }
        }

    }

    /**
     * Strips common suffixes like "Result", "DTO", "Entity" from a class name.
     * @param s the input string
     * @return the stripped string
     */
    private static String stripCommonSuffixes(String s) {
        String[] suffixes = {"Result", "DTO", "Entity"};

        for (String suf : suffixes) {
            if (s.endsWith(suf) && s.length() > suf.length()) {
                return s.substring(0, s.length() - suf.length());
            }
        }

        return s;
    }

    /**
     * Convert a single string to the requested target type.
     */
    private static Object convert(String value, Class<?> targetType) {
        if (value == null)
            return null;

        if (targetType == String.class)
            return value;
        if (targetType == Integer.class || targetType == int.class)
            return Integer.valueOf(value);
        if (targetType == Long.class || targetType == long.class)
            return Long.valueOf(value);
        if (targetType == Boolean.class || targetType == boolean.class)
            return Boolean.valueOf(value);
        if (targetType == Double.class || targetType == double.class)
            return Double.valueOf(value);
        if (targetType == Float.class || targetType == float.class)
            return Float.valueOf(value);
        if (targetType.isEnum())
            return Enum.valueOf((Class<Enum>) targetType, value);
        // Add more conversions if needed (enums, dates, BigInteger, etc.)

        // If targetType is assignable from String (rare), return the raw string
        if (targetType.isAssignableFrom(String.class))
            return value;

        throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getName());
    }
}
