package cake.web.exchange;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import cake.web.exception.AmbiguityException;

public final class PutRequestExchange extends AbstractRequestExchange {
    public PutRequestExchange(HttpServletRequest request) throws IOException {
        super(request);
    }

    @Override
    public Object call() throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException, AmbiguityException {
        return call(HttpMethodName.PUT);
    }
}
