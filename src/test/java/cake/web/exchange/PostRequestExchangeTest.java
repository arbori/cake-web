package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.thebank.loan.entity.CustomerEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;
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
        String bodyJson = "{ \"name\": \"John Doe\", \"email\": \"john.doe@anywhere.com\" }";

        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PostRequestExchange exchange = new PostRequestExchange(request);

        Object result = exchange.call();

        assertTrue(result instanceof CustomerEntity, "Result should be a CustomerEntity");

        CustomerEntity expected = new CustomerEntity().setId(1).setName("John Doe");
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
