package cake.web.exchange;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.bank.loan.CustomerResult;
import com.bank.loan.ProposalResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class PostRequestExchangeTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void postRequestExchangeWithBodyContent() throws Exception {
        String bodyJson = "{ \"name\": \"John Doe\", \"email\": \"john.doe@anywhere.com\" }";

        when(request.getRequestURI()).thenReturn("cakeweb/com/bank/loan/customer");
        when(request.getContextPath()).thenReturn("cakeweb/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PostRequestExchange exchange = new PostRequestExchange(request);

        Object result = exchange.call();

        assertTrue("Result should be a CustomerResult", result instanceof CustomerResult);

        CustomerResult expected = new CustomerResult(1, "John Doe", "john.doe@anywhere.com");
        assertEquals(expected, result);
    }

    @Test
    public void postRequestExchangeWithNestedDTO() throws Exception {
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

        assertTrue("Result should be a ProposalResult", result instanceof ProposalResult);

        ProposalResult expected = new ProposalResult(
                10001,
                new CustomerResult(1, "Rita", "rita@bank.com"),
                25000.0,
                "APPROVED");

        assertEquals(expected, result);
    }
}
