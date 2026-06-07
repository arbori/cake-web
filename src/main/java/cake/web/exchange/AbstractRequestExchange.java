package cake.web.exchange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import cake.web.exception.NotFoundException;
import cake.web.exception.ResourceResolutionException;
import cake.web.resource.MethodHandler;
import cake.web.resource.MethodResolution;

/**
 * Abstract base class for handling HTTP request exchanges.
 * It provides common functionality for processing requests, resolving
 * resources, * Constructs a BaseRequestExchange with the given request.
 * 
 * and invoking HTTP methods.
 */
abstract class AbstractRequestExchange {
    private static final Map<String, Class<?>> resourceCache = new ConcurrentHashMap<>();
    
    private final HttpDataHandle httpDataHandle;
    
    protected final List<String> tokens;
    protected List<Object> pathParams;

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

        if (requestURI == null || requestURI.isEmpty()) {
            throw new IllegalArgumentException("requestURI must be provided.");
        }

        // Protected against null contextPath (though in practice it should not be null)
        if(contextPath == null) {
            contextPath = "";
        }

        this.httpDataHandle = new HttpDataHandle(request);

        this.tokens = tokenizePath(requestURI, contextPath);
        this.pathParams = new ArrayList<>();

        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("No resource tokens found in the request URI.");
        }
    }

    /**
     * Abstract method to be implemented by subclasses to handle the request.
     * 
     * @return the result of the method invocation
     * @throws IllegalArgumentException if method parameters do not match expected types
     * @throws NoSuchMethodException    if no suitable method is found
     * @throws ClassNotFoundException   if no resource class is found for the tokens
     */
    public abstract Object call() 
        throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException;

    /**
     * Resolves the resource chain based on the request tokens and parameters,
     * and invokes the method corresponding to the given HTTP method name.
     * 
     * @param httpMethod the HTTP method name (e.g., "get", "post")
     * @return the result of the method invocation
     * @throws IllegalArgumentException if method parameters do not match expected types
     * @throws NoSuchMethodException    if no suitable method is found
     * @throws ClassNotFoundException   if no resource class is found for the tokens
     */
    protected Object call(HttpMethodName httpMethod) 
        throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException
    {
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
     * @throws ClassNotFoundException    if no resource class is found for the tokens
     * @throws NoSuchMethodException    if a required method is not found during resolution
     * @throws IllegalArgumentException if method parameters do not match expected types
     */
    private Object lookForResource() throws ClassNotFoundException, NoSuchMethodException, IllegalArgumentException {
        Object resource = null;
        StringBuilder fullClassName = new StringBuilder();

        Iterator<String> tokenIterator = tokens.iterator();
        Optional<Class<?>> classFounded;
        String token;

        // First we need to find the root resource, which is the first class that can be loaded from the tokens.
        while(tokenIterator.hasNext() && resource == null) {
            token = tokenIterator.next();
            classFounded = tryLoadClass(fullClassName.toString(), capitalize(token));

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

        // The resource was founded previously. Then, take the next tokens and try to find child resources or path parameters.
        while(resource != null && tokenIterator.hasNext()) {
            token = tokenIterator.next();
            classFounded = tryLoadClass(fullClassName.toString(), capitalize(token));

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
            throw new ClassNotFoundException("No resource found for given URI");
        }

        return resource;
    }

    /**
     * Finds a method in the given resource class that matches the HTTP method name
     * and can accept the provided path parameters.
     * 
     * @param resourceClass  the class to search for the method
     * @param pathParams     the list of path parameters as strings
     * @param httpMethodName the HTTP method name (e.g., "get", "post")
     * @return the matching Method wrapped in MethodResolution, which includes the method and converted arguments
     * @throws NoSuchMethodException    if no suitable method is found
     * @throws IllegalArgumentException if no method matches the parameter types
     */
    private MethodResolution findHttpMethod(Class<?> resourceClass, HttpMethodName httpMethodName)
            throws NoSuchMethodException, IllegalArgumentException {
        MethodResolution methodResolution = MethodHandler.findHttpMethod(resourceClass, httpMethodName, pathParams, httpDataHandle);
            
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

        // Check if last token conteins query parameters and remove them from the token list.
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
     * @throws ResourceResolutionException if instantiation fails
     */
    private Object instantiateResource(Class<?> resourceClass) {
        try {
            return resourceClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
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
    private Optional<Class<?>> tryLoadClass(String prefix, String sufix) {
        String fqcn = (prefix.isEmpty()) ? sufix : prefix + "." + sufix;
         
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
            throw new NotFoundException("Linkage failure loading " + fqcn, e);
        }
    }

    /**
     * Capitalizes the first letter of the string.
     * 
     * @param s the input string
     * @return the capitalized string
     */
    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return "";

        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
