package cake.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import cake.web.exception.DefaultExceptionMapper;
import cake.web.exception.ExceptionMapper;
import cake.web.exchange.GetRequestExchange;
import cake.web.exchange.PostRequestExchange;

public class RootServlet extends HttpServlet {
    private static final long serialVersionUID = -7807285398220322910L;

    private static final ExceptionMapper exceptionMapper = new DefaultExceptionMapper();

    public RootServlet() {
        super(); // default constructor required
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            GetRequestExchange exchange = new GetRequestExchange(request);
            Object result = exchange.call();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result);
        } catch (RuntimeException re) {
            exceptionMapper.handle(re, response);
        } catch (Exception e) {
            exceptionMapper.handle(e, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            PostRequestExchange exchange = new PostRequestExchange(request);
            Object result = exchange.call();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result);
        } catch (RuntimeException re) {
            exceptionMapper.handle(re, response);
        } catch (Exception e) {
            exceptionMapper.handle(e, response);
        }
    }
}
