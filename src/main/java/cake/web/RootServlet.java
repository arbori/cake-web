package cake.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cake.web.exception.DefaultExceptionMapper;
import cake.web.exchange.GetRequestExchange;

public class RootServlet extends HttpServlet {
    private static final long serialVersionUID = -7807285398220322910L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        DefaultExceptionMapper exceptionMapper = new DefaultExceptionMapper();
        
        try {
            GetRequestExchange exchange = new GetRequestExchange(request);
            Object result = exchange.call();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result);
        } catch (Exception ex) {
            exceptionMapper.handle(ex, response);
        }
    }
}