package cake.web.exchange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import jakarta.servlet.http.HttpServletRequest;

import cake.web.exception.HttpMethodException;
import cake.web.resource.MethodResolution;

public class PostRequestExchange extends AbstractRequestExchange {
    public PostRequestExchange(HttpServletRequest request) throws IOException {
        super(request);
    }

    @Override
    public Object call() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, HttpMethodException, ClassNotFoundException {
        Object resource = lookForResource();

        MethodResolution method = findHttpMethod(resource.getClass(), pathParams, HttpMethodName.POST);

        return method.call(resource);
    }
}
