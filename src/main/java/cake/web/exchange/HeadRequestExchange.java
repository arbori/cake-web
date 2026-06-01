package cake.web.exchange;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

public final class HeadRequestExchange extends AbstractRequestExchange {
    public HeadRequestExchange(HttpServletRequest request) throws IOException {
        super(request);
    }

    @Override
    public Object call() throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException {
        return call(HttpMethodName.HEAD);
    }
}
