package cake.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cake.web.exchange.GetRequestExchange;

public class RootServlet extends HttpServlet {
  private static final long serialVersionUID = -7807285398220322910L;

  /**
   * 
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      GetRequestExchange getRequestExchange = new GetRequestExchange(request);

      Object result = getRequestExchange.call();

      response.getWriter().println(result);
    } catch (NoSuchMethodException | IOException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);

      try {
        response.getWriter().println(e.getMessage());
      } catch (IOException e1) {
        response.getWriter().println(e1.getMessage());
      }
    }
  }
}
