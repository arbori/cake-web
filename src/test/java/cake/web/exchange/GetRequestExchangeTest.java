package cake.web.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import business.service.mint.Design;

public class GetRequestExchangeTest {
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFindoutClassForGet() {
		when(request.getRequestURI()).thenReturn("cake-web/business/service/mint/design");
		when(request.getContextPath()).thenReturn("cake-web/");
		when(request.getParameterMap()).thenReturn(new HashMap<String, String>());

		GetRequestExchange getRequestExchange = null;
		String result = null;
		
		try {
			getRequestExchange = new GetRequestExchange(
				request.getRequestURI(),
				request.getContextPath()
					);
		}
		catch(ClassNotFoundException | NoSuchMethodException e) {
			fail(e.getMessage());
		}
		
		try {
			result = getRequestExchange.get(request.getParameterMap());
		}
		catch(NoSuchMethodException e) {
			fail(e.getMessage());
		}
		
		assertEquals("The return of get method is diferent than expected", Design.METHOD_GET_P0, result);
	}

}
