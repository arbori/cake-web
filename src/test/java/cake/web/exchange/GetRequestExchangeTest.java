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

import static org.junit.Assert.*;
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
        Map<String, String[]> parameters = Map.of("name", new String[] { "John Doe" }, "email",
                new String[] { "john.doe@anywhere.com" });

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
    public void getRequestExchangeCustomerAndProposalByPath() throws IOException {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer/1/proposal/100");
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
                100,
                customerResult,
                5000.0,
                "PENDING");

        System.out.println("Expected: " + expected);
        System.out.println("Result:   " + result);

        assertEquals("The return of get method is different than expected", expected, result);
    }

    @Test
    public void getRequestExchangeMethodCachePerformanceAverage() throws IOException {
        Map<String, String[]> parameters = Map.of("name", new String[] { "John Doe" }, "email",
                new String[] { "john.doe@anywhere.com" });

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
            for (int i = 0; i < iterations; i++) {
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
                avgSecond < timeFirst * 1.3);
    }

    /// --- ADDICIONAL TESTS --- ///

    @Test(expected = NoSuchMethodException.class)
    public void getRequestExchangeMissingMethod() throws Exception {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/resourceless/");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);
        exchange.call(); // should fail: missing id
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestExchangeMissingPathParameter() throws Exception {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer/");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);
        exchange.call(); // should fail: missing id
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestExchangeInvalidPathParameterType() throws Exception {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer/abc");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);
        exchange.call(); // should fail: cannot convert "abc" to int
    }

    @Test(expected = ClassNotFoundException.class)
    public void getRequestExchangeUnknownResource() throws Exception {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/unknown/123");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);
        exchange.call();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestExchangeNoMatchingMethod() throws Exception {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer/1/extraParam");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);
        exchange.call();
    }

    @Test
    public void getRequestExchangeEnumParameter() throws Exception {
        when(request.getRequestURI())
                .thenReturn("cakeweb/com/bank/loan/customer/1/proposal/1001/APPROVED");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);

        Object result = exchange.call();
        assertTrue("Result should be a ProposalResult", result instanceof ProposalResult);

        ProposalResult proposal = (ProposalResult) result;

        // ✅ Customer is required, so we validate presence too
        assertEquals("Customer should be attached", 1, (int) proposal.customer().customerId());
        assertEquals("The customer name is different", "John Doe", proposal.customer().name());
        assertEquals("john.doe@universe.com", proposal.customer().email());

        // ✅ Enum/Status parameter
        assertEquals("APPROVED", proposal.status());
    }

    @Test
    public void getRequestExchangeEnumParameterWithoutCustomerShouldFail() throws Exception {
        when(request.getRequestURI())
            .thenReturn("cakeweb/com/bank/loan/proposal/1001/APPROVED"); // 🚫 no customer in path
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);

        try {
            exchange.call();
            fail("Expected exception when customer is missing");
        } catch (Exception e) {
            assertTrue(e.getMessage()!= null && e.getMessage().contains("Customer is required")); // expected
        }
    }
}
