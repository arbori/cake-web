package cake.web.exchange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import jakarta.servlet.http.HttpServletRequest;

import cake.web.exception.HttpMethodException;
import cake.web.resource.MethodResolution;

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
    public Object call() throws InstantiationException, IllegalAccessException, IllegalArgumentException, 
            InvocationTargetException, NoSuchMethodException, HttpMethodException, ClassNotFoundException {
        Object resource = lookForResource();

        MethodResolution method = findHttpMethod(resource.getClass(), pathParams, HttpMethodName.GET);
        
        return method.call(resource);
    }
}
