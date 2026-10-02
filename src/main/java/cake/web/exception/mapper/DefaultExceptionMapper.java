package cake.web.exception.mapper;

import javax.servlet.http.HttpServletResponse;

import cake.web.exception.AmbiguityException;
import cake.web.exception.BusinessException;
import cake.web.exception.FrameworkException;
import cake.web.exception.MethodInvocationException;
import cake.web.exception.PrimitiveNotAllowedException;
import cake.web.exception.ResourceNotFoundException;
import cake.web.exception.ResourceExchangeException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Default implementation of ExceptionMapper that maps exceptions to HTTP
 * responses.
 */
public class DefaultExceptionMapper implements ExceptionMapper {
    private static final Logger LOGGER = Logger.getLogger(ExceptionMapper.class.getName());

    @Override
    public void handle(Throwable ex, HttpServletResponse response) {
        if (ex instanceof BusinessException) {
            // Expected business flow — log concisely at INFO
            LOGGER.log(Level.INFO, "Business exception: {0}", ex.getMessage());
        } else if (ex instanceof FrameworkException) {
            // Unexpected framework error — log full detail
            LOGGER.log(Level.SEVERE, "Framework exception", ex);
        } else {
            // Uncaught — should not happen if exchange wraps properly
            LOGGER.log(Level.SEVERE, "Unhandled exception", ex);
        }

        try {
            switch (ex) {
                case ResourceNotFoundException _ -> {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().println("Resource not found");
                }
                case ResourceExchangeException _ -> {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Internal server error");
                }
                case MethodInvocationException _ -> {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Internal server error");
                }
                case AmbiguityException _ -> {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Internal server error");
                }
                case PrimitiveNotAllowedException _ -> {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Internal server error");
                }
                case FrameworkException _ -> {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Internal server error");
                }
                case BusinessException bre -> {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().println(bre.getMessage());
                }
                default -> {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().println("Unexpected error");
                }
            }
        } catch (IOException ioEx) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ioEx.printStackTrace();
        }
    }
}
