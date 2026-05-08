package cake.web.exchange;

import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;

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
    public Object call() throws IllegalArgumentException, NoSuchMethodException, ClassNotFoundException {
        return call(HttpMethodName.GET);
    }
}
