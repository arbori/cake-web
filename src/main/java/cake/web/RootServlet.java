package cake.web;

import java.io.IOException;
import java.io.PrintWriter;

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
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    try {
      String result = null;

      GetRequestExchange getRequestExchange = new GetRequestExchange(
          request.getRequestURI(),
          request.getContextPath());

      result = getRequestExchange.get(request.getParameterMap());

      PrintWriter responseWriter = response.getWriter();

      responseWriter.println(result);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
