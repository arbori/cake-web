package cake.web.exchange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

public class GetRequestExchange extends AbstractRequestExchange {
    /**
     * Constructs a GetRequestExchange with the given request.
     * 
     * @param request the HttpServletRequest object
     * @throws IOException if an I/O error occurs reading the request body
     * @throws IllegalArgumentException if request is null
     */
    public GetRequestExchange(HttpServletRequest request) throws IOException {
        super(request);
    }

    @Override
    public Object call() throws InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        Object resource = lookForResource();

        if (resource != null) {
            Method httpMethod = findHttpMethod(resource.getClass(), pathParams, HttpMethodName.GET);

            if (httpMethod != null) {
                setAttributes(resource, parameterMap);

                return callHttpMethod(resource, httpMethod, pathParams);
            }
        }

        return null;
    }
}
