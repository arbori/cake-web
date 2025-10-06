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

import cake.web.exception.HttpMethodException;
import cake.web.exception.MethodInvocationException;
import cake.web.exception.ResourceResolutionException;
import cake.web.exchange.content.Convertion;
import cake.web.resource.BaseResource;

/**
 * Abstract base class for handling HTTP request exchanges.
 * It provides common functionality for processing requests, resolving
 * resources, * Constructs a BaseRequestExchange with the given request.
 * 
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
     * @throws IOException              if an I/O error occurs reading the request
     *                                  body
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

        if (request.getReader() != null) {
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
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException,
            HttpMethodException;

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
     * @throws NoSuchMethodException     if no suitable get method is found
     * @throws InvocationTargetException if method invocation fails
     * @throws IllegalArgumentException  if no method matches the parameter types
     * @throws IllegalAccessException    if the class or its nullary constructor is
     *                                   not accessible
     * @throws InstantiationException    if the class that declares the underlying
     *                                   constructor represents an abstract class
     * @throws HttpMethodException       if method invocation fails
     */
    protected Object lookForResource() throws ClassNotFoundException, NoSuchMethodException, IllegalArgumentException {
        Object resource = null;
        StringBuilder fullClassName = new StringBuilder();

        for (String token : tokens) {
            Optional<Class<?>> classFounded = tryLoadClass(fullClassName + "." + capitalize(token));

            // There is no resource.
            if (resource == null) {
                // If no class found, ...
                if (!classFounded.isPresent()) {
                    // ... keep building package name.
                    fullClassName.append(fullClassName.isEmpty() ? "" : ".").append(token);
                }
                // if we have a class, ...
                else {
                    // ... this is the root resource.
                    resource = instantiateResource(classFounded.get());
                }
            }
            // The resource was founded previously.
            else {
                // If other class was not found, ...
                if (!classFounded.isPresent()) {
                    // ... this token is a path parameter.
                    pathParams.add(token);
                }
                // Other resource was founded.
                else {
                    // find get method on parent resource to obtain child parentResource attribute.
                    Method parentResourceGetMethod = findHttpMethod(resource.getClass(), pathParams,
                            HttpMethodName.GET);

                    // call parent's get method to obtain child parentResource attribute.
                    Object parentResourceResult = callHttpMethod(resource, parentResourceGetMethod);

                    // inject parent result into child resource
                    Object childResource = instantiateResource(classFounded.get());

                    injectParent(childResource, parentResourceResult);

                    // if not terminal (there are more resource tokens), the callResult becomes
                    // previousResult for next resource
                    pathParams.clear();

                    // Set the last resource founded as current resource
                    resource = childResource;
                }
            }
        }

        if (resource == null) {
            throw new ClassNotFoundException("No resource found for given URI");
        }

        if(!parameterMap.isEmpty()) {
            setAttributes(resource, parameterMap);
        }

        if(bodyContent != null && !bodyContent.isEmpty() && (resource instanceof BaseResource baseResource)) {
            try {
                baseResource.setBodyContent(bodyContent.toString().trim());
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to parse body content", e);
            }
        }

        return resource;
    }

    /**
     * Finds all methods in the given resource class that match the HTTP method name.
     * 
     * @param resourceClass  the class to search for methods
     * @param pathParams     the list of path parameters as strings
     * @param httpMethodName the HTTP method name (e.g., "get", "post")
     * @return list of matching Methods
     * @throws NoSuchMethodException if no suitable method is found
     */
    protected List<Method> findHttpMethodList(Class<?> resourceClass, HttpMethodName httpMethodName) throws NoSuchMethodException {
        String partialCacheKey = resourceClass.getName() + "#" + httpMethodName;

        List<Method> methodFoundedList = methodCache.entrySet().stream()
            .filter(e -> e.getKey().startsWith(partialCacheKey))
            .map(Map.Entry::getValue)
            .toList();

        if (!methodFoundedList.isEmpty()) {
            return methodFoundedList;
        }

        methodFoundedList = Arrays.stream(resourceClass.getMethods())
                .filter(m -> m.getName().equals(httpMethodName.toString()))
                .toList();

        if (methodFoundedList.isEmpty()) {
            throw new NoSuchMethodException(
                    "No suitable " + httpMethodName + "(...) for class " + resourceClass.getName());
        }

        methodFoundedList.forEach(m -> {
            String cacheKey = resourceClass.getName() + "#" + httpMethodName + "/" + m.getParameterCount();
            methodCache.put(cacheKey, m);
        });

        return methodFoundedList;
    }

    /**
     * Finds a method in the given resource class that matches the HTTP method name
     * and can accept the provided path parameters.
     * 
     * @param resourceClass  the class to search for the method
     * @param pathParams     the list of path parameters as strings
     * @param httpMethodName the HTTP method name (e.g., "get", "post")
     * @return the matching Method, or null if none found
     * @throws NoSuchMethodException    if no suitable method is found
     * @throws IllegalArgumentException if no method matches the parameter types
     */
    protected Method findHttpMethod(Class<?> resourceClass, List<String> pathParams, HttpMethodName httpMethodName)
            throws NoSuchMethodException, IllegalArgumentException {
        List<Method> methodFoundedList = findHttpMethodList(resourceClass, httpMethodName);

        Method methodFounded = methodFoundedList.stream()
                .filter(m -> {
                    Class<?>[] paramTypes = m.getParameterTypes();

                    if( paramTypes.length != pathParams.size()) {
                        return false;
                    }

                    for (int i = 0; i < paramTypes.length; i++) {
                        Convertion.convert(pathParams.get(i), paramTypes[i]);
                    }

                    return true;
                })
                .findFirst()
                .orElse(null);

        if (methodFounded == null) {
            throw new IllegalArgumentException(
                    "No suitable " + httpMethodName + "(...) for class " + resourceClass.getName()
                            + " with " + pathParams.size() + " positional parameters and expected parameters type.");
        }

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
     * Invokes the given method on the resource instance with path parameters.
     * It converts path parameters to the required types before invocation.
     * 
     * @param resource the object instance to invoke the method on
     * @param method   the Method to invoke
     * @return the result of the method invocation
     * @throws MethodInvocationException if invocation fails
     */
    protected Object callHttpMethod(Object resource, Method method) {
        try {
            Object[] args = new Object[method.getParameterCount()];
            for (int i = 0; i < args.length; i++) {
                args[i] = Convertion.convert(pathParams.get(i), method.getParameterTypes()[i]);
            }
            pathParams.clear();

            return method.invoke(resource, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                // Business exception → propagate unchanged
                throw re;
            }
            // Otherwise infra
            throw new MethodInvocationException("Error invoking method: " + method.getName(), cause);
        } catch (ReflectiveOperationException e) {
            throw new MethodInvocationException("Failed to invoke method: " + method.getName(), e);
        }
    }

    /// ---------- PRIVATE UTILITIES ---------- ///

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
     * Instantiates a resource class using its public no-arg constructor.
     * 
     * @param resourceClass the Class to instantiate
     * @return the instantiated object
     * @throws ResourceResolutionException if instantiation fails
     */
    private Object instantiateResource(Class<?> resourceClass) {
        try {
            return resourceClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ResourceResolutionException(
                    "Resource class " + resourceClass.getName() + " must have a public no-arg constructor.", e);
        }
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
    public static void trySetAttributes(String name, String value, Class<?> clazz, Object instance) {
        String setterName = "set" + capitalize(name);

        // try setter methods first
        try {
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equalsIgnoreCase(setterName) || m.getParameterCount() != 1)
                    continue;
                Class<?> paramType = m.getParameterTypes()[0];
                Object converted = Convertion.convert(value, paramType);
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
            Object converted = Convertion.convert(value, f.getType());
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
}
