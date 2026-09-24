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
import java.util.List;
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
            .setState("Blue Cloud");

        String bodyJson = 
            """
            {
                "addressRequest": {
                    "zipcode":"123.456",
                    "street":"All Green Street",
                    "city":"Big Apple",
                    "state":"Blue Cloud"
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
    void createArrayOfAddressTest() throws Exception {
        String bodyJson = 
            """
            [
                {
                    "zipcode":"123.456",
                    "street":"All Green Street",
                    "city":"Big Apple",
                    "state":"Blue Cloud"
                },
                {
                    "zipcode":"583.951",
                    "street":"Main Avenue",
                    "city":"Downtown",
                    "state":"Big State"
                }
            ]
            """;

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address/");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PostRequestExchange exchange = new PostRequestExchange(request);

        Object result = exchange.call();

        assertNotNull(result);
        assertTrue(result instanceof List<?>, "Result should be a List");
        
        @SuppressWarnings("unchecked")
        List<AddressResponse> retrieved = (List<AddressResponse>) result;

        assertEquals(2, retrieved.size());
        assertEquals(AddressResponse.class, retrieved.get(0).getClass(), "Result should be a list of AddressResponse");
        assertEquals("123.456", retrieved.get(0).getZipcode());
        assertEquals("583.951", retrieved.get(1).getZipcode());

        retrieved.forEach(ar -> assertNotNull(ar.getId())); 
    }

    @Test
    void createListOfCustomerTest() throws Exception {
        // Ensure address exists for customer creation
        loanService.createAddress(
            "123.456", 
            "All Green Street", 
            "Big Apple", 
            "Blue Cloud");

        String bodyJson = 
            """
            [
                {
                    "name": "Customer One",
                    "salary": 3000.00,
                    "addressRequest": {
                        "zipcode": "123.456",
                        "street": "All Green Street",
                        "city": "Big Apple",
                        "state": "Blue Cloud"
                    }
                },
                {
                    "name": "Customer Two",
                    "salary": 4500.00,
                    "addressRequest": {
                        "zipcode": "123.456",
                        "street": "All Green Street",
                        "city": "Big Apple",
                        "state": "Blue Cloud"
                    }
                }
            ]
            """;

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/customer/");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PostRequestExchange exchange = new PostRequestExchange(request);

        Object result = exchange.call();

        assertNotNull(result);
        assertTrue(result instanceof List<?>, "Result should be a List");

        @SuppressWarnings("unchecked")
        List<CustomerResponse> retrieved = (List<CustomerResponse>) result;

        assertEquals(2, retrieved.size());
        assertEquals("Customer One", retrieved.get(0).getName());
        assertEquals(3000.00, retrieved.get(0).getSalary());
        assertNotNull(retrieved.get(0).getId());
        assertNotNull(retrieved.get(0).getAddressResponse());

        assertEquals("Customer Two", retrieved.get(1).getName());
        assertEquals(4500.00, retrieved.get(1).getSalary());
        assertNotNull(retrieved.get(1).getId());
        assertNotNull(retrieved.get(1).getAddressResponse());
    }

    @Test
    void failCreateListOfCustomerWithoutAddressTest() throws Exception {
        String bodyJson = 
            """
            [
                {
                    "name": "Invalid Customer",
                    "salary": 1500.00,
                    "addressRequest": {
                        "zipcode": "000.000",
                        "street": "Nonexistent Street",
                        "city": "Nowhere",
                        "state": "Void"
                    }
                }
            ]
            """;

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/customer/");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PostRequestExchange exchange = new PostRequestExchange(request);

        assertThrowsExactly(BadRequestException.class, exchange::call);
    }

    @Test
    void failCreateCustomerWithoutAddressTest() throws Exception {
        String bodyJson = 
            """
            {
                "customerRequest": {
                    "name":"John None",
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
            "Blue Cloud");
        
        CustomerResponse customerExpected = new CustomerResponse()
            .setName("John None")
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
