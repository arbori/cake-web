package cake.web.exchange;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

class BaseRequestExchange {
    protected final String requestURI;
    protected final List<String> tokens;

    protected BaseRequestExchange(String requestURI, String contextPath) {
        if (requestURI == null || requestURI.isEmpty() || contextPath == null || contextPath.isEmpty()) {
            throw new IllegalArgumentException("requestURI and contextPath must be provided.");
        }

        this.requestURI = requestURI;
        this.tokens = tokenizePath(requestURI, contextPath);
    }

    /* --------------------- Core chain resolver --------------------- */
    protected Optional<Object> resolveChainWithBase(
            String basePackage,
            int startIndex,
            Map<String, String[]> queryParameters,
            String body,
            String methodName) throws ReflectiveOperationException {

        if (startIndex >= tokens.size()) {
            throw new ClassNotFoundException("No resource token after base package: " + basePackage);
        }

        Object previousResult = null;
        int i = startIndex;

        while (i < tokens.size()) {
            String resourceToken = tokens.get(i);
            String candidateClassName = basePackage + "." + capitalize(resourceToken);

            Class<?> resourceClass = tryLoadClass(candidateClassName)
                    .orElseThrow(() -> new ClassNotFoundException("Resource class not found: " + candidateClassName));

            // collect path params destined for this resource until next token that looks
            // like a class
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

            // set query params on resource instance BEFORE calling method
            setAttributes(resourceInstance, queryParameters);

            // find the method suitable for this set of pathParams
            Method targetMethod = findMethod(resourceClass, methodName, pathParams);
            if (targetMethod == null) {
                throw new NoSuchMethodException(
                        "No suitable " + methodName + "(...) for class " + resourceClass.getName()
                                + " with " + pathParams.size() + " positional parameters.");
            }

            // call the method
            Object callResult = callMethod(resourceInstance, targetMethod, pathParams, body);

            previousResult = callResult;
            i = j;
        }

        return Optional.ofNullable(previousResult);
    }

    /* --------------------- Helpers --------------------- */

    protected List<String> tokenizePath(String uri, String contextPath) {
        return Arrays.stream(uri.substring(uri.indexOf(contextPath) + contextPath.length()).split("/"))
                .filter(s -> !s.isEmpty())
                .toList();
    }

    protected Optional<Class<?>> tryLoadClass(String fqcn) {
        try {
            return Optional.of(Class.forName(fqcn));
        } catch (ClassNotFoundException _) {
            return Optional.empty();
        }
    }

    protected boolean classExists(String fqcn) {
        return tryLoadClass(fqcn).isPresent();
    }

    protected static String join(List<String> parts, String sep) {
        return parts.stream().collect(Collectors.joining(sep));
    }

    protected static String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    protected void setAttributes(Object instance, Map<String, String[]> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        Class<?> clazz = instance.getClass();
        params.forEach((name, values) -> {
            String value = (values != null && values.length > 0) ? values[0] : null;
            if (value != null) {
                trySetAttributes(name, value, clazz, instance);
            }
        });
    }

    private void trySetAttributes(String name, String value, Class<?> clazz, Object instance) {
        String setterName = "set" + capitalize(name);
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
            // Do not set an attribute has no consequence.
        }

        try {
            Field f = clazz.getDeclaredField(name);
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
            VarHandle handle = lookup.unreflectVarHandle(f);
            Object converted = convert(value, f.getType());
            handle.set(instance, converted);
        } catch (Exception _) {
            // Do not set an attribute has no consequence.
        }
    }

    protected static Method findMethod(Class<?> resourceClass, String methodName, List<String> pathParams) {
        Method[] methods = Arrays.stream(resourceClass.getMethods())
                .filter(m -> m.getName().equals(methodName))
                .toArray(Method[]::new);

        for (Method method : methods) {
            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != pathParams.size()) {
                continue;
            }
            boolean convertible = true;
            for (int i = 0; i < paramTypes.length; i++) {
                try {
                    convert(pathParams.get(i), paramTypes[i]);
                } catch (Exception _) {
                    convertible = false;
                    break;
                }
            }
            if (convertible) {
                return method;
            }
        }
        return null;
    }

    protected static Object callMethod(Object controllerInstance, Method method, List<String> pathParams, String body)
            throws InvocationTargetException, IllegalAccessException {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            args[i] = convert(pathParams.get(i), paramTypes[i]);
        }
        return method.invoke(controllerInstance, args);
    }

    protected static void injectParent(Object childInstance, Object parentResult) {
        if (parentResult == null) {
            return;
        }
        Class<?> childClass = childInstance.getClass();
        Class<?> parentClass = parentResult.getClass();

        String parentSimple = parentClass.getSimpleName();
        String stripped = stripCommonSuffixes(parentSimple);

        List<String> candidateSetterNames = new ArrayList<>();
        candidateSetterNames.add("set" + parentSimple);
        candidateSetterNames.add("set" + stripped);

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
                // Do not set an attribute has no consequence.
            }
        }

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
            // Do not set an attribute has no consequence.
        }

        for (Field f : childClass.getDeclaredFields()) {
            if (f.getType().isAssignableFrom(parentClass)) {
                try {
                    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(childClass, MethodHandles.lookup());
                    VarHandle handle = lookup.unreflectVarHandle(f);
                    handle.set(childInstance, parentResult);
                } catch (IllegalAccessException _) {
                    // Do not set an attribute has no consequence.
                }
            }
        }
    }

    protected static String stripCommonSuffixes(String s) {
        String[] suffixes = { "Result", "DTO", "Entity" };
        for (String suf : suffixes) {
            if (s.endsWith(suf) && s.length() > suf.length()) {
                return s.substring(0, s.length() - suf.length());
            }
        }
        return s;
    }

    protected static Object convert(String value, Class<?> targetType) {
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
        throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getName());
    }
}
