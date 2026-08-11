package cake.web.exchange;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import cake.web.exception.AmbiguityException;

public final class DeleteRequestExchange extends AbstractRequestExchange {
    public DeleteRequestExchange(HttpServletRequest request) throws IOException {
        super(request);
    }

    @Override
    public Object call() throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException, AmbiguityException {
        return call(HttpMethodName.DELETE);
    }
}
