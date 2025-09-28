package cake.web.exchange;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

/**
 * Abstract base class for handling HTTP request exchanges.
 * It provides common functionality for processing requests, resolving resources,
 * and invoking HTTP methods.
 */
abstract class AbstractRequestExchange {
    private static final Map<String, Class<?>> resourceCache = new ConcurrentHashMap<>();
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    protected final List<String> tokens;
    protected List<String> pathParams;
    protected Map<String, String[]> parameterMap;
    protected StringBuilder bodyContent;

    /**
     * Constructs a BaseRequestExchange with the given request.
     * It tokenizes the path and initializes internal state.
     * 
     * @param request the HttpServletRequest object
     * @throws IOException if an I/O error occurs reading the request body
     * @throws IllegalArgumentException if requestURI or contextPath are null/empty
     */
    AbstractRequestExchange(HttpServletRequest request) throws IOException {
        String requestURI = request.getRequestURI(); // Extract the path from the URI
        String contextPath = request.getContextPath(); // Assuming contextPath is part of the path

        if (requestURI == null || requestURI.isEmpty() || contextPath == null || contextPath.isEmpty()) {
            throw new IllegalArgumentException("requestURI and contextPath must be provided.");
        }

        this.tokens = tokenizePath(requestURI, contextPath);
        this.pathParams = new ArrayList<>();
        this.parameterMap = request.getParameterMap();
        this.bodyContent = new StringBuilder();

        if(request.getReader() != null) {
            request.getReader().lines().forEach(line -> bodyContent.append(line).append("\n"));
        }

        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("No resource tokens found in the request URI.");
        }
    }

    /**
     * Processes the request by resolving the resource chain and invoking the
     * appropriate http method.
     * 
     * @return the result of the http method invocation, or null if not found
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public abstract Object call() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException;

    /**
     * Recursively resolves the resource chain based on URI tokens and path
     * parameters.
     * It builds package names, loads classes, and invokes parent get methods as
     * needed.
     *
     * @param tokens         the list of remaining URI tokens
     * @param pathParams     the list of collected path parameters
     * @param parentResource the current parent resource instance
     * @return the final resolved resource instance or null if not found
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    protected Object lookForResource() throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        Object resource = null;
        StringBuilder packageName = new StringBuilder();
        
        for(String token: tokens) {
            String nextToken = isPackageInClasspath(packageName, token);

            if(nextToken != null) {
                packageName.append(packageName.isEmpty() ? "" : ".").append(nextToken);
            } else {
                nextToken = token;

                Optional<Class<?>> classNameFounded = tryLoadClass(packageName + "." + capitalize(nextToken));

                // if we have a class but no parent, this is the root resource
                if (resource == null && classNameFounded.isPresent()) {
                    resource = classNameFounded.get().getDeclaredConstructor().newInstance();
                }
                // if we have a parent but no class, this token is a path param
                else if (resource != null && !classNameFounded.isPresent()) {
                    pathParams.add(nextToken);
                }
                // if we have both parent and class, we need to call parent's get to obtain

                else if (resource != null) {
                    // find get method on parent resource to obtain child parentResource attribute.
                    Method parentResourceGetMethod = findHttpMethod(resource.getClass(), pathParams, HttpMethodName.GET);

                    // if no suitable get method, it means the path params do not match
                    if (parentResourceGetMethod == null) {
                        throw new NoSuchMethodException("No suitable get(...) for class " + resource.getClass().getName()
                                + " with " + pathParams.size() + " positional parameters.");
                    }

                    // call parent's get method to obtain child parentResource attribute.
                    Object parentResourceResult = callHttpMethod(resource, parentResourceGetMethod, pathParams);

                    // inject parent result into child resource
                    Object childResource = classNameFounded.get().getDeclaredConstructor().newInstance();

                    injectParent(childResource, parentResourceResult);

                    // if not terminal (there are more resource tokens), the callResult becomes
                    // previousResult for next resource
                    pathParams.clear();

                    // Set the last resource founded as current resource
                    resource = childResource;
                }
            }
        }

        return resource;
    }

    /**
     * Finds a method in the given resource class that matches the HTTP method name
     * and can accept the provided path parameters.
     * 
     * @param resourceClass the class to search for the method
     * @param pathParams    the list of path parameters as strings
     * @param httpMethodName the HTTP method name (e.g., "get", "post")
     * @return the matching Method, or null if none found
     */
    protected Method findHttpMethod(Class<?> resourceClass, List<String> pathParams, HttpMethodName httpMethodName) {
        String cacheKey = resourceClass.getName() + "#" + httpMethodName + "/" + pathParams.size();

        if (methodCache.containsKey(cacheKey)) {
            return methodCache.get(cacheKey);
        }

        Method methodFounded = Arrays.stream(resourceClass.getMethods())
                .filter(m -> m.getName().equals(httpMethodName.toString()))
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
        
        methodCache.put(cacheKey, methodFounded);

        return methodFounded;
    }

    /**
     * Sets attributes on the given instance using the provided parameters map.
     * It tries to find setter methods first, then falls back to direct field
     * access.
     * Only the first value of each parameter is used.
     * 
     * @param instance the object instance to set attributes on
     * @param params   the map of parameter names to values
     */
    protected void setAttributes(Object instance, Map<String, String[]> params) {
        if (params == null || params.isEmpty()) {
            return;
        }

        Class<?> clazz = instance.getClass();

        params.forEach((name, values) -> {
            String value;

            if ((value = (values != null && values.length > 0) ? values[0] : null) != null) {
                trySetAttributes(name, value, clazz, instance);
            }
        });
    }

    /**
     * Calls the given http method on the resource instance with the provided path
     * parameters.
     * 
     * @param resource the instance of the resource class
     * @param method             the http method to invoke
     * @param pathParams         the list of path parameters as strings
     * @return the result of the method invocation
     * @throws InvocationTargetException if the underlying method throws an
     *                                   exception
     * @throws IllegalAccessException    if this Method object is enforcing Java
     *                                   language access control and the underlying
     *                                   method is inaccessible
     */
    protected Object callHttpMethod(Object resource, Method method, List<String> pathParams)
            throws InvocationTargetException, IllegalAccessException {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];

        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = convert(pathParams.get(i), paramTypes[i]);
        }

        pathParams.clear();

        return method.invoke(resource, args);
    }

    /// ---------- PRIVATE UTILITIES ---------- ///

    /**
     * Checks if the package formed by appending nextToken to packageName exists in
     * the classpath.
     * If it exists, appends nextToken to packageName.
     *
     * @param packageName the current package name being built
     * @param nextToken   the next token to append
     * @return true if the package exists in the classpath, false otherwise
     */
    private static String isPackageInClasspath(StringBuilder packageName, String nextToken) {
        // Build candidate class FQCN safely (no leading dot)
        String current = packageName.isEmpty() ? "" : packageName.toString();
        String candidateClass = current.isEmpty()
                ? capitalize(nextToken)
                : current + "." + capitalize(nextToken);

        // If a class with that name exists, then this token is likely a class, not a
        // package
        if (tryLoadClass(candidateClass).isPresent()) {
            return null;
        }

        // Build the new package name we want to test
        String newPackage = current.isEmpty() ? nextToken : current + "." + nextToken;

        // Use context class loader; check resource path for the package
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        return classLoader.getResource(newPackage.replace('.', '/') + "/") != null
            ? nextToken : null;
    }

    /**
     * Tokenizes the path after the contextPath, splitting on '/' and ignoring empty
     * tokens.
     * 
     * @param uri         the full request URI
     * @param contextPath the context path to strip
     * @return list of path tokens
     */
    private static List<String> tokenizePath(String uri, String contextPath) {
        String path = uri.startsWith(contextPath)
                ? uri.substring(contextPath.length())
                : uri;

        return Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .toList();
    }

    /**
     * Attempts to load a class by fully qualified class name.
     * 
     * @param fqcn the fully qualified class name
     * @return Optional containing the Class if found, or empty if not found
     */
    private static Optional<Class<?>> tryLoadClass(String fqcn) {
        Class<?> classFounded = resourceCache.get(fqcn);

        if (classFounded != null) {
            return Optional.of(classFounded);
        }

        try {
            classFounded = Class.forName(fqcn);

            resourceCache.put(fqcn, classFounded);

            return Optional.of(classFounded);
        } catch (ClassNotFoundException _) {
            return Optional.empty();
        }
    }

    /**
     * Tries to set a single attribute on the given instance by name and value.
     * It first attempts to find a setter method, then falls back to direct field
     * access.
     * 
     * @param name     the attribute name
     * @param value    the attribute value as string
     * @param clazz    the class of the instance
     * @param instance the object instance to set the attribute on
     */ 
    private static void trySetAttributes(String name, String value, Class<?> clazz, Object instance) {
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
     * Capitalizes the first letter of the string.
     * 
     * @param s the input string
     * @return the capitalized string
     */
    private static String capitalize(String s) {
        if (s == null || s.isEmpty())
            return "";

        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Attempts to inject parentResult into childInstance:
     * - Prefer setter whose parameter type is assignable from
     * parentResult.getClass()
     * and whose name matches set<ParentName> (matching stripped suffixes).
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
     * 
     * @param s the input string
     * @return the stripped string
     */
    private static String stripCommonSuffixes(String s) {
        String[] suffixes = { "Result", "DTO", "Entity" };

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
