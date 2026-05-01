package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.bank.loan.CustomerResult;
import com.bank.loan.ProposalResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

class GetRequestExchangeTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getRequestExchangeById() throws IOException {
        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/customer/1");
        when(request.getContextPath()).thenReturn("thebank.com/");

        GetRequestExchange getRequestExchange = new GetRequestExchange(request);

        Object result = null;

        try {
            result = getRequestExchange.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(result);
    }

    @Test
    void getRequestExchangeQueryParameter() throws IOException {
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

        assertEquals(customerExpected, result, "The return of get method is different than expected");
    }

    @Test
    void getRequestExchangePathParameter() throws IOException {
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
        assertEquals(customerExpected, result, "The return of get method is different than expected");
    }

    @Test
    void getRequestExchangeCustomerAndProposalByPath() throws IOException {
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

        assertEquals(expected, result, "The return of get method is different than expected");
    }

    @Test
    void getRequestExchangeMethodCachePerformanceAverage() throws IOException {
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

                assertEquals(expected, firstResult, "Unexpected result in first call");
                assertEquals(expected, secondResult, "Unexpected result in second call");
            }
        } catch (Exception e) {
            fail("Iteration failed: " + e.getMessage());
        }

        double avgSecond = totalSecond / iterations;

        assertTrue(avgSecond < timeFirst * 1.3,
                String.format("Expected cached calls to be faster or similar (timeFirst=%fns, avgSecond=%fns)",
                        timeFirst, avgSecond));
    }

    /// --- ADDICIONAL TESTS --- ///

    @Test
    void getRequestExchangeMissingMethod() throws Exception {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/resourceless/");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);

        assertThrows(NoSuchMethodException.class, exchange::call); // should fail: missing id
    }

    @Test
    void getRequestExchangeMissingPathParameter() throws Exception {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer/");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);
        
        assertThrows(IllegalArgumentException.class, exchange::call); // should fail: missing id
    }

    @Test
    void getRequestExchangeInvalidPathParameterType() throws Exception {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer/abc");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);
        assertThrows(IllegalArgumentException.class, exchange::call); // should fail: cannot convert "abc" to int
    }

    @Test
    void getRequestExchangeUnknownResource() throws Exception {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/unknown/123");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);
        assertThrows(ClassNotFoundException.class, exchange::call);
    }

    @Test
    void getRequestExchangeNoMatchingMethod() throws Exception {
        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer/1/extraParam");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);
        assertThrows(NoSuchMethodException.class, exchange::call);
    }

    @Test
    void getRequestExchangeEnumParameter() throws Exception {
        when(request.getRequestURI())
                .thenReturn("cakeweb/com/bank/loan/customer/1/proposal/1001/APPROVED");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);

        Object result = exchange.call();
        assertTrue(result instanceof ProposalResult, "Result should be a ProposalResult");

        ProposalResult proposal = (ProposalResult) result;

        // ✅ Customer is required, so we validate presence too
        assertEquals(1, (int) proposal.customer().customerId(), "Customer should be attached");
        assertEquals("John Doe", proposal.customer().name(), "The customer name is different");
        assertEquals("john.doe@universe.com", proposal.customer().email(), "The customer email is different");

        // ✅ Enum/Status parameter
        assertEquals("APPROVED", proposal.status());
    }

    @Test
    void getRequestExchangeEnumParameterWithoutCustomerShouldFail() throws Exception {
        when(request.getRequestURI())
            .thenReturn("cakeweb/com/bank/loan/proposal/1001/APPROVED"); // 🚫 no customer in path
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);

        try {
            exchange.call();
            fail("Expected exception when customer is missing");
        } catch (Exception e) {
            assertTrue(e.getMessage()!= null && e.getMessage().contains("Customer is required"), "Expected exception when customer is missing"); // expected
        }
    }
}
