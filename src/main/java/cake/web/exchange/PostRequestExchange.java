package cake.web.exchange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;

import cake.web.exception.HttpMethodException;

public class PostRequestExchange extends AbstractRequestExchange {
    public PostRequestExchange(HttpServletRequest request) throws IOException {
        super(request);
    }

    @Override
    public Object call() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, HttpMethodException, ClassNotFoundException {
        Object resource = lookForResource();

        Method method = findHttpMethod(resource.getClass(), pathParams, HttpMethodName.POST);

        return callHttpMethod(resource, method);
    }
}
