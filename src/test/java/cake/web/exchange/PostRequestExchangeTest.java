package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.thebank.loan.entity.CustomerEntity;

import loan.capture.CustomerResponse;

import java.io.*;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
class PostRequestExchangeTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void postRequestExchangeWithBodyContent() throws Exception {
        String bodyJson = "{\"customerRequest\":{ \"name\": \"John Doe\", \"salary\": \"15000.0\"}}";

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/customer/");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PostRequestExchange exchange = new PostRequestExchange(request);

        Object result = exchange.call();

        assertTrue(result instanceof CustomerResponse, "Result should be a CustomerResponse");

        CustomerResponse retrieved = (CustomerResponse) result;
        CustomerResponse expected = new CustomerResponse().setName("John Doe").setSalary(15000.0);
        expected.setId(retrieved.getId()); // ID is generated, so we set it to match the result for equality check
        
        assertEquals(expected, result);
    }

    /*
     --- IGNORE ---
     @Test
     void postRequestExchangeWithMissingBody() throws Exception {
         when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer");
         when(request.getContextPath()).thenReturn("cakeweb/");
         when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));
         when(request.getParameterMap()).thenReturn(Map.of());

         PostRequestExchange exchange = new PostRequestExchange(request);

         try {
             exchange.call();
             fail("Expected an exception to be thrown due to missing body");
         } catch (Exception e) {
             assertTrue(e.getMessage() != null && e.getMessage().contains("No body in request"), "Expected exception when body is missing"); // expected
         }
    }
    
    @Test
    void postRequestExchangeWithNestedDTO() throws Exception {
        String bodyJson = """
                {
                    "amount": 25000,
                    "status": "APPROVED",
                    "customer": {
                        "name": "Rita",
                        "email": "rita@bank.com"
                    }
                }
                """;

        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/proposal");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PostRequestExchange exchange = new PostRequestExchange(request);
        Object result = exchange.call();

        assertTrue(result instanceof ProposalResult, "Result should be a ProposalResult");

        ProposalResult expected = new ProposalResult(
                100,
                new CustomerResult(1, "Rita", "rita@bank.com"),
                25000.0,
                "APPROVED");

        assertEquals(expected, result);
    }
    */
}
