package cake.web.exchange;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;

public final class PutRequestExchange extends AbstractRequestExchange {
    public PutRequestExchange(HttpServletRequest request) throws IOException {
        super(request);
    }

    @Override
    public Object call() throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException {
        return call(HttpMethodName.PUT);
    }
}

