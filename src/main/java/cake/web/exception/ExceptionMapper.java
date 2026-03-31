package cake.web.exception;

import javax.servlet.http.HttpServletResponse;

/**
 * Interface for mapping exceptions to HTTP responses.
 */
public interface ExceptionMapper {
    /**
     * Handles the given exception and maps it to an appropriate HTTP response.
     *
     * @param ex       The exception to handle.
     * @param response The HttpServletResponse to write the response to.
     */
    void handle(Throwable ex, HttpServletResponse response);
}
