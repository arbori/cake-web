package cake.web.exchange;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.util.*;

public class GetRequestExchangeRecursive {
    private final List<String> tokens;

    public GetRequestExchangeRecursive(String requestURI, String contextPath) {
        if (requestURI == null || requestURI.isEmpty() || contextPath == null || contextPath.isEmpty()) {
            throw new IllegalArgumentException("requestURI and contextPath must be provided.");
        }

        this.tokens = tokenizePath(requestURI, contextPath);
    }

    /**
     * Main entry point to process the GET request.
     * It resolves the resource chain, finds the appropriate get method,
     * sets attributes from query parameters, and invokes the method.
     *
     * @param parameterMap the map of query parameters
     * @return the result of the GET method invocation
     * @throws InvocationTargetException if the underlying method throws an exception
     * @throws IllegalAccessException if this Method object is enforcing Java language access control and the underlying method is inaccessible
     */
    public Object call(Map<String, String[]> parameterMap) throws InvocationTargetException, IllegalAccessException {
        List<String> uriTokens = new ArrayList<>(this.tokens);
        List<String> pathParams = new ArrayList<>();

        Object resource = lookForResource(uriTokens, pathParams, null);

        if(resource != null) {
            Method getMethod = findGetMethod(resource.getClass(), pathParams, "get");

            if(getMethod != null) {
                setAttributes(resource, parameterMap);

                return callGetMethod(resource, getMethod, pathParams);
            }
        }

        return null;
    }

    /* --------------------- Core chain resolver --------------------- */

    /**
     * Recursively resolves the resource chain based on URI tokens and path parameters.
     * It builds package names, loads classes, and invokes parent get methods as needed.
     *
     * @param uriTokens the list of remaining URI tokens
     * @param pathParams the list of collected path parameters
     * @param parentResource the current parent resource instance
     * @return the final resolved resource instance or null if not found
     */
    private Object lookForResource(List<String> uriTokens, List<String> pathParams, Object parentResource) {
        if(uriTokens == null || uriTokens.isEmpty() || pathParams == null) {
            return parentResource;
        }

        StringBuilder packageName = new StringBuilder(parentResource != null ? parentResource.getClass().getPackageName() : "");

        String nextToken = "";

        while(!uriTokens.isEmpty() && isPackageInClasspath(packageName, (nextToken = uriTokens.remove(0)))) {
            // keep consuming tokens as long as they form a valid package
        }

        Optional<Class<?>> classNameFounded = tryLoadClass(packageName + "." + capitalize(nextToken));

        // if we have no parent but no class, we cannot proceed
        if(parentResource == null && !classNameFounded.isPresent()) {
            return null;
        }
        // if we have a class but no parent, this is the root resource
        else if(parentResource == null) {
            try {
                parentResource = classNameFounded.get().getDeclaredConstructor().newInstance();

                return lookForResource(uriTokens, pathParams, parentResource);
            } catch (ReflectiveOperationException | IllegalArgumentException _) {
                return null;
            }
        }
        // if we have a parent but no class, this token is a path param
        else if(!classNameFounded.isPresent()) {
            pathParams.add(nextToken);
            return lookForResource(uriTokens, pathParams, parentResource);
        } 
        // if we have both parent and class, we need to call parent's get to obtain child parentResource attribute
        else {
            try {
                // find get method on parent resource to obtain child parentResource attribute.
                Method parentResourceGetMethod = findGetMethod(parentResource.getClass(), pathParams, "get");

                // if no suitable get method, it means the path params do not match
                if(parentResourceGetMethod == null) {
                    throw new NoSuchMethodException("No suitable get(...) for class " + parentResource.getClass().getName()
                        + " with " + pathParams.size() + " positional parameters.");
                }

                // call parent's get method to obtain child parentResource attribute.
                Object parentResourceResult = callGetMethod(parentResource, parentResourceGetMethod, pathParams);

                // inject parent result into child resource
                Object childResource = classNameFounded.get().getDeclaredConstructor().newInstance();

                injectParent(childResource, parentResourceResult);

                // if not terminal (there are more resource tokens), the callResult becomes previousResult for next resource
                pathParams.clear();

                return lookForResource(uriTokens, pathParams, childResource);
            } catch (ReflectiveOperationException | IllegalArgumentException _) {
                return null;
            }
        }
    }

    /**
     * Checks if the package formed by appending nextToken to packageName exists in the classpath.
     * If it exists, appends nextToken to packageName.
     *
     * @param packageName the current package name being built
     * @param nextToken   the next token to append
     * @return true if the package exists in the classpath, false otherwise
     */
    public boolean isPackageInClasspath(StringBuilder packageName, String nextToken) {        
        if(tryLoadClass(packageName + "." + capitalize(nextToken)).isPresent()) {
            return false;
        }

        packageName.append(packageName.isEmpty() ? "" : ".").append(nextToken);
        
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        return classLoader.getResource(packageName.toString().replace('.', '/') + '/') != null;
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
        } catch (ClassNotFoundException _) {
            return Optional.empty();
        }
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
    private static Method findGetMethod(Class<?> resourceClass, List<String> pathParams, String httpMethod) {
        return Arrays.stream(resourceClass.getMethods())
            .filter(m -> m.getName().equals(httpMethod)) // e.g., "get"
            .filter(m -> m.getParameterCount() == pathParams.size())
            .filter(m -> {
                Class<?>[] paramTypes = m.getParameterTypes();

                for (int i = 0; i < paramTypes.length; i++) {
                    try {
                        convert(pathParams.get(i), paramTypes[i]);
                    } catch (Exception _) {
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
                // try next candidate
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
            // try next candidate
        }

        // third pass: try fields
        for (Field f : childClass.getDeclaredFields()) {
            if (f.getType().isAssignableFrom(parentClass)) {
                try {
                    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(childClass, MethodHandles.lookup());
                    VarHandle handle = lookup.unreflectVarHandle(f);
                    handle.set(childInstance, parentResult);

                    return;
                } catch (ReflectiveOperationException _) {
                    // try next field
                }
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