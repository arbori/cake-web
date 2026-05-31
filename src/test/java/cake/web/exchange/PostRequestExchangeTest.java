package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
}
