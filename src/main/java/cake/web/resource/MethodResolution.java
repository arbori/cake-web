package cake.web.resource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import cake.web.exception.MethodInvocationException;

/**
 * A simple record to hold the result of method resolution, including the method and its arguments.
 */
public record MethodResolution(Method method, List<Object> args) {
    /**
     * Invokes the method on the resource instance using the converted arguments.
     * 
     * @param resource the object instance to invoke the method on
     * @return the result of the method invocation
     * @throws MethodInvocationException if invocation fails
     */
    public Object call(Object resource) throws MethodInvocationException {
        try {
            return method.invoke(resource, args.toArray());
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
}
