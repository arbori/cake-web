package cake.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import business.service.mint.Design;

public class RootServletTest {
	@Spy
	private RootServlet rootServlet;
	@Mock
	private ServletConfig servletConfig;
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private PrintWriter printWriter;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void callGetMethodWithouParameters() throws Exception {
		when(rootServlet.getServletConfig()).thenReturn(servletConfig);
		when(response.getWriter()).thenReturn(printWriter);
		when(request.getRequestURI()).thenReturn("cake-web/business/service/mint/design");
		when(request.getContextPath()).thenReturn("cake-web/");
		
		rootServlet.doGet(request, response);
		
		verify(response.getWriter()).println(Design.METHOD_GET_P0);
	}

	@Test
	public void callGetMethodWithId() throws Exception {
		when(rootServlet.getServletConfig()).thenReturn(servletConfig);
		when(response.getWriter()).thenReturn(printWriter);
		when(request.getRequestURI()).thenReturn("cake-web/business/service/mint/design");
		when(request.getContextPath()).thenReturn("cake-web/");
		
		HashMap<String, String[]> paramMap = new HashMap<>();
		paramMap.put("id", new String[] { "123" });
		when(request.getParameterMap()).thenReturn(paramMap);
		
		rootServlet.doGet(request, response);
		
		verify(printWriter).println(Design.METHOD_GET_P1);
	}
}
