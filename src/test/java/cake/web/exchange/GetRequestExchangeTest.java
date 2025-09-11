package cake.web.exchange;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.bank.loan.CustomerResult;

public class GetRequestExchangeTest {
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testFindoutClassForGet() {
		when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer");
		when(request.getContextPath()).thenReturn("cakeweb/");
		when(request.getParameterMap()).thenReturn(Map.of("customerId", new String[] {"1"}));

		GetRequestExchange getRequestExchange = null;
		Object result = null;
		
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
		
		CustomerResult customerExpected = new CustomerResult(1, "John Doe", "john.doe@universe.com");

		assertEquals("The return of get method is diferent than expected", customerExpected, result);
	}
}
