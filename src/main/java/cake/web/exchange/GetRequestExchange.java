package cake.web.exchange;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetRequestExchange {
    private Class<?> getClass;
    private List<Method> getMethods;

    /**
     * 
     * @param requestURI
     * @param contextPath
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     */
    public GetRequestExchange(String requestURI, String contextPath)
            throws ClassNotFoundException, NoSuchMethodException {
        getClass = extractClass(requestURI, contextPath);

        getMethods = extractGetMethod(getClass);
    }

    /**
     * Call the get method of the controller class mapping parameters from the URI.
     * 
     * @param uriParameters the parameters from the URI
     * 
     * @return the result of the get method as a String
     * 
     * @throws NoSuchMethodException
     */
    public Object get(Map<String, String[]> uriParameters) throws NoSuchMethodException {
        Method getMethod = null;

        for (Method method : getMethods) {
            if ("get".equals(method.getName())) {
                Double similarity = (double) (uriParameters.keySet().size() - method.getParameters().length);

                if (similarity.intValue() == 0) {
                    getMethod = method;
                }
            }
        }

        if (getMethod == null) {
            throw new NoSuchMethodException("There is no get method with the set of parameters recieved.");
        }

        try {
            Object result = callGetMethod(getClass, getMethod, uriParameters);

            // TODO: Here is important to check content type to return JSON, XML, HTML, etc.
            return result;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            // TODO: A propper return take in considearation http status codes.
            e.printStackTrace();

            return e;
        }
    }

    /**
     * Extract the class from the URI and context path.
     * 
     * @param uri the request URI
     * @param contextPath the context path
     * 
     * @return the extracted class
     * 
     * @throws ClassNotFoundException
     */
    private Class<?> extractClass(String uri, String contextPath) throws ClassNotFoundException {
        if (uri == null || uri.isEmpty() || contextPath == null || contextPath.isEmpty()) {
            throw new ClassNotFoundException("Correspondent class not found.");
        }

        String uriClassName = uri.substring(uri.indexOf(contextPath) + contextPath.length(), uri.length())
                .replace("/", ".");

        int letterToUpperPossition = uriClassName.lastIndexOf(".") + 1;

        byte[] uriClassNameBytes = uriClassName.getBytes();

        if (uriClassNameBytes[letterToUpperPossition] > (byte) 'Z') {
            uriClassNameBytes[letterToUpperPossition] -= (byte) 32;
        }

        return Class.forName(new String(uriClassNameBytes));
    }

    /**
     * Extract the get method from the provided class.
     * 
     * @param getClass the class to extract the get method from
     * 
     * @return a list of get methods
     * 
     * @throws NoSuchMethodException
     */
    private List<Method> extractGetMethod(Class<?> getClass) throws NoSuchMethodException {
        if (getClass == null) {
            throw new NoSuchMethodException("The get class is null.");
        }

        ArrayList<Method> result = new ArrayList<>();

        Method[] methods = getClass.getMethods();

        for (Method method : methods) {
            String methodName = method.getName();

            if ("get".equals(methodName)) {
                result.add(method);
            }
        }

        if (result.isEmpty()) {
            throw new NoSuchMethodException("Get method not found.");
        }

        return result;
    }

    /**
     * Call the get method of the controller class mapping parameters from the URI.
     * 
     * @param controller the controller class
     * @param getMethod the get method to be called
     * @param uriParameters the parameters from the URI
     * 
     * @return the result of the get method
     * 
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    private static Object callGetMethod(Class<?> controller, Method getMethod, Map<String, String[]> uriParameters) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        // Extract method parameter types
        Class<?>[] paramTypes = getMethod.getParameterTypes();

        // Convert Map<String, Object> to ordered List<String> values
        List<String> orderedValues = mapValuesToList(uriParameters);

        // Validate parameter count
        if (paramTypes.length != orderedValues.size()) {
            throw new IllegalArgumentException("Mismatch between URI parameters and method arguments.");
        }

        // Convert values to correct types
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = convert(orderedValues.get(i), paramTypes[i]);
        }

        // Instantiate controller and invoke method
        Object controllerInstance = controller.getDeclaredConstructor().newInstance();
        return getMethod.invoke(controllerInstance, args);
    }

    /**
     * Map URI parameters to an ordered list of values.
     * 
     * @param uriParameters the parameters from the URI
     * 
     * @return a list of parameter values in order
     */
    private static List<String> mapValuesToList(Map<String, String[]> uriParameters) {
        return uriParameters.entrySet().stream()
            .map(e -> (e.getValue() != null && e.getValue().length > 0) ? e.getValue()[0] : "")
            .filter(value -> !value.isEmpty())
            .toList();
    }

    /**
     * Convert a string value to the specified target type.
     * 
     * @param value the string value to convert
     * @param targetType the target class type
     * 
     * @return the converted object
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
        // Extend with more types as needed

        throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getName());
    }
}
