package cake.web.exchange;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import cake.web.exception.AmbiguityException;
import cake.web.exception.MethodInvocationException;
import cake.web.exception.ResourceExchangeException;
import cake.web.exception.ResourceNotFoundException;

public final class HeadRequestExchange extends AbstractRequestExchange {
    public HeadRequestExchange(HttpServletRequest request) throws IOException, ResourceExchangeException {
        super(request);
    }

    @Override
    public Object call() throws AmbiguityException, ResourceExchangeException, MethodInvocationException, ResourceNotFoundException {
        return call(HttpMethodName.HEAD);
    }
}
