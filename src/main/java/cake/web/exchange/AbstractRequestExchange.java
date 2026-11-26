package cake.web.exchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import cake.web.configuration.CakeWebRegistry;
import cake.web.exception.AmbiguityException;
import cake.web.exception.FrameworkException;
import cake.web.exception.MethodInvocationException;
import cake.web.exception.ResourceNotFoundException;
import cake.web.exception.ResourceExchangeException;
import cake.web.exchange.content.StyleCase;
import cake.web.resource.MethodHandler;
import cake.web.resource.MethodResolution;

/**
 * <p>
 * Base class for handling HTTP request exchanges in the Cake Web framework.
 * </p>
 * 
 * <p>
 * This class orchestrates the entire request processing flow:
 * </p>
 * <ol>
 * <li>Parses the request URI into tokens</li>
 * <li>Extracts request data (body, query params, headers) via
 * {@link HttpDataHandle}</li>
 * <li>Resolves the resource class chain from URI tokens</li>
 * <li>Instantiates resources using their public no-arg constructors</li>
 * <li>Injects parent resource results into child resources</li>
 * <li>Resolves the target method via {@link MethodHandler}</li>
 * <li>Invokes the method with converted parameters</li>
 * </ol>
 * 
 * <h3>Parent Resource Injection</h3>
 * <p>
 * When a URI has a nested resource chain (e.g.,
 * {@code /customer/123/proposal/456}),
 * the framework automatically resolves each parent resource and passes its
 * result
 * as the first parameter to the child resource's method. This enables natural
 * navigation of resource hierarchies.
 * </p>
 * 
 * <h3>Design Intention</h3>
 * <p>
 * This class embodies the framework's core convention: <b>URI path → Java
 * package/class</b>.
 * By following naming conventions, developers can build REST APIs without
 * configuration.
 * </p>
 * 
 * <h3>Thread Safety</h3>
 * <p>
 * This class is not thread-safe. A new instance is created per request.
 * </p>
 * 
 * @since 0.0.45
 * @author Marcelo Arbori Nogueira (marcelo.arbori@gmial.com)
 * @see HttpMethodName
 * @see MethodHandler
 * @see HttpDataHandle
 */
abstract class AbstractRequestExchange {
    private static final Map<String, Class<?>> resourceCache = new ConcurrentHashMap<>();

    private final HttpDataHandle httpDataHandle;

    private final String requestURI;
    private final String contextPath;
    protected final List<String> tokens;
    protected final List<Object> pathParams;

    /**
     * Constructs a BaseRequestExchange with the given request.
     * It tokenizes the path and initializes internal state.
     * 
     * @param request the HttpServletRequest object
     * @throws IOException               if an I/O error occurs reading the request
     *                                   body
     * @throws ResourceExchangeException if requestURI or contextPath are null/empty
     */
    AbstractRequestExchange(HttpServletRequest request) throws ResourceExchangeException, IOException {
        // Extract the path from the URI
        requestURI = request.getRequestURI();
        // Assuming contextPath is part of the path
        contextPath = (request.getContextPath() != null) ? request.getContextPath() : "";

        if (requestURI == null || requestURI.isEmpty()) {
            throw new ResourceExchangeException("requestURI must be provided.");
        }

        pathParams = new ArrayList<>();

        httpDataHandle = new HttpDataHandle(request);

        tokens = tokenizePath(requestURI, contextPath);
        if (tokens.isEmpty()) {
            throw new ResourceExchangeException("Invalid request URI: [" + requestURI + "/" + contextPath + "]");
        }
    }

    /**
     * Abstract method to be implemented by subclasses to handle the request.
     * 
     * @throws ResourceNotFoundException if a resource class was not found at all
     * @throws ResourceExchangeException if no resource class is found for the
     *                                   tokens
     * @throws MethodInvocationException if no suitable method is found or method
     *                                   parameters do not match expected types
     * @throws AmbiguityException        there are more than one method with the
     *                                   same number of parameters into resource
     *                                   class
     * 
     * @return the result of the method invocation
     */
    public abstract Object call() throws ResourceNotFoundException, ResourceExchangeException, MethodInvocationException, AmbiguityException;

    /**
     * Resolves the resource chain based on the request tokens and parameters,
     * and invokes the method corresponding to the given HTTP method name.
     * 
     * @param httpMethod the HTTP method name (e.g., "get", "post")
     * 
     * @return the result of the method invocation
     * 
     * @throws ResourceNotFoundException if a resource class was not found at all
     * @throws ResourceExchangeException if no resource class is found for the
     *                                   tokens
     * @throws MethodInvocationException if no suitable method is found or method
     *                                   parameters do not match expected types
     * @throws AmbiguityException        there are more than one method with the
     *                                   same number of parameters into resource
     *                                   class
     */
    protected Object call(HttpMethodName httpMethod)
            throws ResourceExchangeException, MethodInvocationException, AmbiguityException, ResourceNotFoundException {
        Object resource = lookForResource();

        MethodResolution methodResolution = findHttpMethod(resource.getClass(), httpMethod);

        return methodResolution.call(resource);
    }

    /**
     * Resolves the resource chain based on the request tokens and parameters.
     * It iteratively tries to load classes corresponding to the path tokens,
     * instantiating resources and invoking parent get methods for parent resources
     * to pass the result as parameter for the child resource.
     * 
     * @return the resolved resource object
     * 
     * @throws ResourceNotFoundException if a resource class was not found at all
     * @throws ResourceExchangeException if the exchange from URI to some resource
     *                                   class fail
     * @throws MethodInvocationException if no suitable method is found or method
     *                                   parameters do not match expected types
     * @throws AmbiguityException        there are more than one method with the
     *                                   same number of parameters into resource
     *                                   class
     */
    private Object lookForResource() throws ResourceExchangeException, MethodInvocationException, AmbiguityException, ResourceNotFoundException {
        try {
            Object resource = null;
            StringBuilder fullClassName = new StringBuilder();

            Iterator<String> tokenIterator = tokens.iterator();
            Optional<Class<?>> classFounded;
            String token;

            // First we need to find the root resource, which is the first class that can be
            // loaded from the tokens.
            while (tokenIterator.hasNext() && resource == null) {
                token = tokenIterator.next();

                String className = StyleCase.toPascalCase(token);

                classFounded = tryLoadClass(fullClassName.toString(), className);

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

            // The resource was founded previously. Then, take the next tokens and try to
            // find child resources or path parameters.
            while (resource != null && tokenIterator.hasNext()) {
                token = tokenIterator.next();

                String className = StyleCase.toPascalCase(token);

                classFounded = tryLoadClass(fullClassName.toString(), className);

                // The resource was founded previously.
                // If other class was not found, ...
                if (!classFounded.isPresent()) {
                    // ... this token is a path parameter.
                    pathParams.add(token);
                }
                // Other resource was founded.
                else {
                    // find get method on parent resource to obtain child parentResource attribute.
                    MethodResolution parentResourceGetMethod = findHttpMethod(resource.getClass(), HttpMethodName.GET);

                    // call parent's get method to obtain child parentResource attribute.
                    Object parentResourceResult = parentResourceGetMethod.call(resource);

                    // put parent result as parameter for child resource resolution (if any)
                    pathParams.add(parentResourceResult);

                    // inject parent result into child resource
                    resource = instantiateResource(classFounded.get());
                }
            }

            if (resource == null) {
                throw new ResourceNotFoundException("URI " + requestURI + " does not mach to any resource");
            }

            return resource;
        } catch (NoSuchElementException e) {
            throw new ResourceExchangeException("Inconsistent URI: " + requestURI, e);
        }
    }

    /**
     * Finds a method in the given resource class that matches the HTTP method name
     * and can accept the provided path parameters.
     * 
     * @param resourceClass  the class to search for the method
     * @param pathParams     the list of path parameters as strings
     * @param httpMethodName the HTTP method name (e.g., "get", "post")
     * @return the matching Method wrapped in MethodResolution, which includes the
     *         method and converted arguments
     * @throws MethodInvocationException if no suitable method is found or no method
     *                                   matches the parameter types
     * @throws AmbiguityException        there are more than one method with the
     *                                   same number of parameters into resource
     *                                   class
     */
    private MethodResolution findHttpMethod(Class<?> resourceClass, HttpMethodName httpMethodName)
            throws MethodInvocationException, AmbiguityException {
        MethodResolution methodResolution;

        try {
            methodResolution = MethodHandler.findHttpMethod(resourceClass, httpMethodName, pathParams,
                    httpDataHandle);
        } catch (NoSuchMethodException | IllegalArgumentException e) {
            throw new MethodInvocationException(
                    "Method " + resourceClass.getName() + "." + httpMethodName + ", was not found.", e);
        }

        pathParams.clear();

        return methodResolution;
    }

    /**
     * Tokenizes the path after the contextPath, splitting on '/' and ignoring empty
     * tokens.
     * 
     * @param uri         the full request URI
     * @param contextPath the context path to strip
     * @return list of path tokens
     */
    private List<String> tokenizePath(String uri, String contextPath) {
        String path = uri.startsWith(contextPath)
                ? uri.substring(contextPath.length())
                : uri;

        List<String> tokenized = Arrays.asList(path.split("/"));

        // Check if last token conteins query parameters and remove them from the token
        // list.
        if (!tokenized.isEmpty()) {
            String lastToken = tokenized.get(tokenized.size() - 1);

            if (lastToken.contains("?")) {
                tokenized.set(tokenized.size() - 1, lastToken.substring(0, lastToken.indexOf("?")));
            }
        }

        return tokenized;
    }

    /**
     * Instantiates a resource class using its public no-arg constructor.
     * 
     * @param resourceClass the Class to instantiate
     * @return the instantiated object
     * @throws ResourceExchangeException if instantiation fails
     */
    private Object instantiateResource(Class<?> resourceClass) throws ResourceExchangeException {
        try {
            return resourceClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new ResourceExchangeException(
                    "Resource class " + resourceClass.getName() + " must have a public no-arg constructor.", e);
        }
    }

    /**
     * Attempts to load a class by fully qualified class name.
     * 
     * @param fqcn the fully qualified class name
     * @return Optional containing the Class if found, or empty if not found
     */
    private Optional<Class<?>> tryLoadClass(String prefix, String sufix) {
        String fqcn = (prefix.isEmpty()) ? sufix : prefix + "." + sufix;

        // 🔒 Security: enforce base package restriction
        if (!CakeWebRegistry.isClassLoadAllowed(fqcn)) {
            return Optional.empty();
        }

        Class<?> classFounded = resourceCache.get(fqcn);

        if (classFounded != null) {
            return Optional.of(classFounded);
        }

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            classFounded = Class.forName(fqcn, false, classLoader);

            resourceCache.put(fqcn, classFounded);

            return Optional.of(classFounded);
        } catch (ClassNotFoundException _) {
            return Optional.empty();
        } catch (LinkageError e) {
            throw new FrameworkException("Linkage failure loading " + fqcn, e);
        }
    }
}
