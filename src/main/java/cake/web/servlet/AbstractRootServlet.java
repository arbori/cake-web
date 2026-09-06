package cake.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract base class for servlets that handle HTTP requests.
 * <p>
 * Each supported HTTP method is mapped to a dedicated exchange class. The
 * servlet handles successful responses with HTTP 200 and delegates exception
 * handling to the shared {@link ExceptionMapper} instance.
 * </p>
 * 
 * @since 0.0.45
 * @author Marcelo Arbori Nogueira (marcelo.arbori@gmial.com) 
 */
class AbstractRootServlet extends HttpServlet {

    /**
     * Handle HTTP PATCH requests.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws ServletException if the request could not be handled
     * @throws IOException if an I/O error occurs
     */
    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String protocol = request.getProtocol();
        String msg = "PATCH method is not implemented yet.";

        if (protocol.endsWith("1.1")) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
    }

    /**
     * Handle HTTP CONNECT requests.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws ServletException if the request could not be handled
     * @throws IOException if an I/O error occurs
     */
    protected void doConnect(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String protocol = request.getProtocol();
        String msg = "CONNECT method is not implemented yet.";

        if (protocol.endsWith("1.1")) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
    }

    /**
     * Override the default service method to handle PATCH and CONNECT requests.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @throws ServletException if the request could not be handled
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        switch (request.getMethod()) {
            case "PATCH":
                this.doPatch(request, response);
                break;
            case "CONNECT":
                this.doConnect(request, response);
                break;
            default:
                super.service(request, response);
        }
    }
}
