package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import loan.capture.AddressResponse;
import loan.capture.CustomerResponse;
import loan.capture.ProposalResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thebank.loan.service.LoanService;

class GetRequestExchangeTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    static LoanService loanService = new LoanService();

    List<AddressResponse> addresses;
    List<CustomerResponse> customers;

    @BeforeAll
    static void beforeAll() {
        AddressResponse addr = loanService.createAddress("123-456", "Rua dos Afogados, 23", "São Paulo", "São Paulo");
        loanService.createCustomer("João da Silva", 1000.00, addr.getId());

        addr = loanService.createAddress("171-666", "Rua Alada, 17", "Rio de Janeiro", "Rio de Janeiro");
        loanService.createCustomer("Maria Oliveira", 1500.00, addr.getId());


        addr = loanService.createAddress("456-7686", "Avenida Alada, 1007", "Rio Prado", "Goias");
        loanService.createCustomer("João Maria Oliveira", 2500.00, addr.getId());
        loanService.createCustomer("Josefina Oliveira", 1500.00, addr.getId());
        loanService.createCustomer("Mauro Oliveira", 500.00, addr.getId());
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        addresses = loanService.getAllAddresses();
        customers = loanService.getAllCustomers();
    }

    @Test
    void getRequestExchangeById() throws IOException {
        CustomerResponse customer = loanService.getCustomer(customers.get(0).getId())
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/customer/" + customer.getId());
        when(request.getContextPath()).thenReturn("thebank.com/");

        GetRequestExchange getRequestExchange = new GetRequestExchange(request);

        Object result = null;

        try {
            result = getRequestExchange.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(result);
        assertEquals(customer, result);
    }

    @Test
    void getRequestExchangeChildResource() throws Exception {
        CustomerResponse customerResponse = customers.get(0);

        ProposalResponse proposalResponse = loanService.requestLoan(
            customerResponse.getId(), 
            customerResponse.getSalary(), 
            12,
            0.02, 
            LocalDate.now());

        when(request.getRequestURI())
                .thenReturn("thebank.com/loan/capture/customer/" + customerResponse.getId() + "/proposal/" + proposalResponse.getId());
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange exchange = new GetRequestExchange(request);

        Object result = exchange.call();
        assertTrue(result instanceof ProposalResponse, "Result should be a ProposalResponse");

        ProposalResponse proposal = (ProposalResponse) result;

        // ✅ Customer is required, so we validate presence too
        assertEquals(proposalResponse.getId(), (int) proposal.getId(), "The proposal id is different");
        assertEquals(customerResponse.getId(), (int) proposal.getCustomerId(), "The customer id is different");
    }

    @Test
    void getRequestExchangeQueryParameter() throws IOException {
        String cityName = "Rio Prado";

        List<CustomerResponse> rioPradoCustomers = loanService.getCustomersByCity(cityName);

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/customer");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(
            Map.of(
                "city", new String[] { cityName },
                "minimumSalary", new String[] { "1500.00" },
                "maximumSalary", new String[] { "2500.00" }
            )
        );

        GetRequestExchange getRequestExchange = new GetRequestExchange(request);

        Object result = null;
        try {
            result = getRequestExchange.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertEquals(rioPradoCustomers, result, "The return of get method is different than expected");
    }

    /*
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
    */
}
