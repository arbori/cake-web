package cake.web.exchange;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.bank.loan.CustomerResult;
import com.bank.loan.ProposalResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

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
    public void getRequestExchangeQueryParameter() throws IOException {
		Map<String, String[]> parameters = Map.of("name", new String[]{"John Doe"}, "email", new String[]{"john.doe@anywhere.com"});

		when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer/1");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(parameters);

        GetRequestExchange getRequestExchange = new GetRequestExchange(request);

        Object result = null;
        try {
            result = getRequestExchange.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        CustomerResult customerExpected = new CustomerResult(1, parameters.get("name")[0], parameters.get("email")[0]);
		
        assertEquals("The return of get method is different than expected", customerExpected, result);
    }

    @Test
    public void getRequestExchangePathParameter() throws IOException {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer/1");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange getRequestExchange = new GetRequestExchange(request);

        Object result = null;
        try {
            result = getRequestExchange.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        CustomerResult customerExpected = new CustomerResult(1, "John Doe", "john.doe@universe.com");
        assertEquals("The return of get method is different than expected", customerExpected, result);
    }
	
    @Test
    public void getRequestExchangePathParameterTwoResources() throws IOException {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer/1/proposal/P-1001");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange getRequestExchange = new GetRequestExchange(request);

        Object result = null;
        try {
            result = getRequestExchange.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        CustomerResult customerResult = new CustomerResult(1, "John Doe", "john.doe@universe.com");
        ProposalResult expected = new ProposalResult(
            "P-1001", 
            customerResult, 
            10000.0, 
            "Analizing");

        assertEquals("The return of get method is different than expected", expected, result);
    }
	
	@Test
	public void getRequestExchangeMethodCachePerformanceAverage() throws IOException {
		Map<String, String[]> parameters = Map.of("name", new String[]{"John Doe"}, "email", new String[]{"john.doe@anywhere.com"});

		when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer/1");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(parameters);

		GetRequestExchange getRequestExchange = new GetRequestExchange(request);

        CustomerResult expected = new CustomerResult(1, parameters.get("name")[0], parameters.get("email")[0]);

		// Warm up JIT
		try {
			getRequestExchange.call();
		} catch (Exception e) {
			fail("Warmup failed: " + e.getMessage());
		}

		int iterations = 20;

		double timeFirst = System.nanoTime();
		double totalSecond = 0;

		try {
			// Warm up cache
			for(int i = 0; i < iterations; i++) {
				getRequestExchange.call();
			}

			Object firstResult = null;
			Object secondResult = null;

			timeFirst = System.nanoTime();
			firstResult = getRequestExchange.call();
			timeFirst = System.nanoTime() - timeFirst;

			for (int i = 0; i < iterations; i++) {
				double start2 = System.nanoTime();
				secondResult = getRequestExchange.call();
				totalSecond += (double) System.nanoTime() - start2;

				assertEquals("Unexpected result in first call", expected, firstResult);
				assertEquals("Unexpected result in second call", expected, secondResult);
			}
		} catch (Exception e) {
			fail("Iteration failed: " + e.getMessage());
		}

		double avgSecond = totalSecond / iterations;

		assertTrue(
			String.format("Expected cached calls to be faster or similar (timeFirst=%fns, avgSecond=%fns)",
				timeFirst, avgSecond),
			avgSecond < timeFirst
		);
	}
}
