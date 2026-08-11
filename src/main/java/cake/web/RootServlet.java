package cake.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cake.web.exception.DefaultExceptionMapper;
import cake.web.exception.ExceptionMapper;
import cake.web.exchange.ConnectRequestExchange;
import cake.web.exchange.DeleteRequestExchange;
import cake.web.exchange.GetRequestExchange;
import cake.web.exchange.HeadRequestExchange;
import cake.web.exchange.OptionsRequestExchange;
import cake.web.exchange.PatchRequestExchange;
import cake.web.exchange.PostRequestExchange;
import cake.web.exchange.PutRequestExchange;
import cake.web.exchange.TraceRequestExchange;

/**
 * Servlet implementation that routes HTTP requests to the corresponding
 * exchange handlers defined in the application.
 * <p>
 * Each supported HTTP method is mapped to a dedicated exchange class. The
 * servlet handles successful responses with HTTP 200 and delegates exception
 * handling to the shared {@link ExceptionMapper} instance.
 * </p>
 * 
 * @since 0.0.45
 * @author Marcelo Arbori Nogueira (marcelo.arbori@gmial.com) 
 */
public class RootServlet extends HttpServlet {
    private static final long serialVersionUID = -7807285398220322910L;

    private static final ExceptionMapper exceptionMapper = new DefaultExceptionMapper();

    /**
     * Create a new RootServlet instance.
     */
    public RootServlet() {
        super(); // default constructor required
    }

    /**
     * Handle HTTP GET requests.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

    /**
     * Handle HTTP HEAD requests.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     */
    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

    /**
     * Handle HTTP POST requests.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

    /**
     * Handle HTTP PUT requests.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

    /**
     * Handle HTTP DELETE requests.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

    /**
     * Handle HTTP CONNECT requests.
     * <p>
     * This method is not part of the {@link HttpServlet} base implementation,
     * but it is defined by the HTTP specification and is supported by this
     * servlet through a dedicated exchange handler.
     * </p>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     */
    protected void doConnect(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            ConnectRequestExchange exchange = new ConnectRequestExchange(request);
            Object result = exchange.call();

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(result);
        } catch (RuntimeException re) {
            exceptionMapper.handle(re, response);
        } catch (Exception e) {
            exceptionMapper.handle(e, response);
        }
    }

    /**
     * Handle HTTP OPTIONS requests.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

    /**
     * Handle HTTP TRACE requests.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     */
    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
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

    /**
     * Handle HTTP PATCH requests.
     * <p>
     * This method is not part of the {@link HttpServlet} base implementation,
     * but patch semantics are handled through a dedicated exchange class.
     * </p>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     */
    protected void doPath(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            PatchRequestExchange exchange = new PatchRequestExchange(request);
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
