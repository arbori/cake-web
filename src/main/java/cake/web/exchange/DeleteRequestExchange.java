package cake.web.exchange;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

public final class DeleteRequestExchange extends AbstractRequestExchange {
    public DeleteRequestExchange(HttpServletRequest request) throws IOException {
        super(request);
    }

    @Override
    public Object call() throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException {
        return call(HttpMethodName.DELETE);
    }
}
