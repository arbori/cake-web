package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.thebank.loan.model.AddressResponse;
import com.thebank.loan.model.CustomerResponse;
import com.thebank.loan.service.LoanService;

import cake.web.exception.BadRequestException;

import java.io.*;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
class PostRequestExchangeTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private LoanService loanService = new LoanService();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAddressTest() throws Exception {
        AddressResponse expected = new AddressResponse()
            .setZipcode("123.456")
            .setStreet("All Green Street")
            .setCity("Big Apple")
            .setState("Blue Clound");

        String bodyJson = 
            """
            {
                "addressRequest": {
                    "zipcode":"123.456",
                    "street":"All Green Street",
                    "city":"Big Apple",
                    "state":"Blue Clound"
                }
            }                    
            """;

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address/");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PostRequestExchange exchange = new PostRequestExchange(request);

        Object result = exchange.call();

        assertTrue(result instanceof AddressResponse, "Result should be a AddressResponse");

        // ID is generated, so we set it to match the result for equality check
        AddressResponse retrieved = (AddressResponse) result;
            
        expected.setId(retrieved.getId()); 
        
        assertEquals(expected, result);
    }

    @Test
    void failCreateCustomerWithouAddressTest() throws Exception {
        String bodyJson = 
            """
            {
                "customerRequest": {
                    "name":"John Noone",
                    "salary":1500.00,
                    "addressRequest": {
                        "zipcode":"999.999",
                        "street":"Empty Street",
                        "city":"Bright City",
                        "state":"Big State"
                    }
                }
            }                    
            """;

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/customer/");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PostRequestExchange exchange = new PostRequestExchange(request);

        assertThrowsExactly(BadRequestException.class, exchange::call);
    }

    @Test
    void createCustomerWithAddress() throws Exception {
        AddressResponse addressExpected = loanService.createAddress(
            "123.456", 
            "All Green Street", 
            "Big Apple", 
            "Blue Clound");
        
        CustomerResponse customerExpected = new CustomerResponse()
            .setName("John Noone")
            .setSalary(1500.00)
            .setAddressResponse(addressExpected);

        String bodyJson = new StringBuilder()
            .append("{\n")
                .append("\"customerRequest\": {\n")
                    .append("\"name\": \"").append(customerExpected.getName()).append("\",\n")
                    .append("\"salary\": ").append(customerExpected.getSalary()).append(",\n")
                    .append("\"addressRequest\": {\n")
                        .append("\"zipcode\": \"").append(addressExpected.getZipcode()).append("\",\n")
                        .append("\"street\": \"").append(addressExpected.getStreet()).append("\",\n")
                        .append("\"city\": \"").append(addressExpected.getCity()).append("\",\n")
                        .append("\"state\": \"").append(addressExpected.getState()).append("\"\n")
                    .append("}\n")
                .append("}\n")
            .append("}\n")
            .toString();

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/customer/");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PostRequestExchange exchange = new PostRequestExchange(request);

        Object result = exchange.call();

        assertNotNull(result);
        assertTrue(result instanceof CustomerResponse);

        CustomerResponse customerResponse = (CustomerResponse) result;

        assertNotNull(customerResponse.getAddressResponse());
        addressExpected.setId(customerResponse.getAddressResponse().getId());
        
        assertNotNull(customerResponse.getId());
        customerExpected.setId(customerResponse.getId());

        assertEquals(customerExpected, customerResponse);
    }
}
