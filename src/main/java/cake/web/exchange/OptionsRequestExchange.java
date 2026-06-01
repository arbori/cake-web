package cake.web.exchange;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

public class OptionsRequestExchange extends AbstractRequestExchange {
    public OptionsRequestExchange(HttpServletRequest request) throws IOException {
        super(request);
    }

    @Override
    public Object call() throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException {
        return call(HttpMethodName.CONNECT);
    }
}
