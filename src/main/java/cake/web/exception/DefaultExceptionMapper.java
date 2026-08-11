package cake.web.exception;

import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Default implementation of ExceptionMapper that maps exceptions to HTTP responses.
 */
public class DefaultExceptionMapper implements ExceptionMapper {
    @Override
    public void handle(Throwable ex, HttpServletResponse response) {
        try {
            switch (ex) {
                case NotFoundException nfe -> {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().println(nfe.getMessage());
                }
                case BadRequestException bre -> {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println(bre.getMessage());
                }
                case BusinessException be -> {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.getWriter().println(be.getMessage());
                }
                case FrameworkException fe -> {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Framework error: " + fe.getMessage());
                }
                case HttpMethodException hme -> {
                    response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    response.getWriter().println("Invalid HTTP method: " + hme.getMessage());
                }
                default -> {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Unexpected error: " + ex.getMessage());
                }
            }
        } catch (IOException ioEx) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ioEx.printStackTrace();
        }
    }
}
