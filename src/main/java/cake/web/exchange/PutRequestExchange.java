package cake.web.exchange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import cake.web.exception.HttpMethodException;
import cake.web.resource.MethodResolution;

public final class PutRequestExchange extends AbstractRequestExchange {
    public PutRequestExchange(HttpServletRequest request) throws IOException {
        super(request);
    }

    @Override
    public Object call() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, HttpMethodException, ClassNotFoundException {
        Object resource = lookForResource();

        MethodResolution methodResolution = findHttpMethod(resource.getClass(), pathParams, HttpMethodName.PUT);

        return methodResolution.call(resource);
    }
}

