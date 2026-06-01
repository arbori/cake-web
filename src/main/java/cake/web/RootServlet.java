package cake.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cake.web.exception.DefaultExceptionMapper;
import cake.web.exception.ExceptionMapper;
import cake.web.exchange.DeleteRequestExchange;
import cake.web.exchange.GetRequestExchange;
import cake.web.exchange.HeadRequestExchange;
import cake.web.exchange.OptionsRequestExchange;
import cake.web.exchange.PostRequestExchange;
import cake.web.exchange.PutRequestExchange;
import cake.web.exchange.TraceRequestExchange;

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
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HeadRequestExchange exchange = new HeadRequestExchange(request);
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

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            PutRequestExchange exchange = new PutRequestExchange(request);
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
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            DeleteRequestExchange exchange = new DeleteRequestExchange(request);
            Object result = exchange.call();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result);
        } catch (RuntimeException re) {
            exceptionMapper.handle(re, response);
        } catch (Exception e) {
            exceptionMapper.handle(e, response);
        }
    }

    // This method is not provided by base class. But, it is defined by http specification and its behavior is pretty 
    // different that others method implemented here. So, this behavior will be implemented in the future. 
    protected void doConnect(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            OptionsRequestExchange exchange = new OptionsRequestExchange(request);
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
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            TraceRequestExchange exchange = new TraceRequestExchange(request);
            Object result = exchange.call();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result);
        } catch (RuntimeException re) {
            exceptionMapper.handle(re, response);
        } catch (Exception e) {
            exceptionMapper.handle(e, response);
        }
    }

    // This method is not provided by base class. The behavior is similar to the put. This behavior will be implemented in the future. 
    protected void doPath(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new UnsupportedOperationException();
    }
}
