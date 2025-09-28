package cake.web.exchange;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class PostRequestExchange extends AbstractRequestExchange {
    public PostRequestExchange(HttpServletRequest request) throws IOException {
        super(request);
    }

    @Override
    public Object call() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException {
        Object resource = lookForResource();

        if (resource != null) {
            Method httpMethod = findHttpMethod(resource.getClass(), pathParams, HttpMethodName.POST);

            if (httpMethod != null) {
                setAttributes(resource, parameterMap);

                if(this.bodyContent != null && !this.bodyContent.isEmpty()) {
                    setAttributes(resource, Map.of("bodyContent", new String[] { this.bodyContent.toString() }));
                }

                return callHttpMethod(resource, httpMethod, pathParams);
            }
        }

        return null;
    }

}
