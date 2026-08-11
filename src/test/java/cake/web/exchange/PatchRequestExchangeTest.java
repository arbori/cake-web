package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.thebank.loan.model.AddressResponse;
import com.thebank.loan.service.LoanService;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class PatchRequestExchangeTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    static LoanService loanService = new LoanService();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void patchRequestShouldPartiallyUpdateResource() throws Exception {
        // Create initial address
        AddressResponse original = loanService.createAddress("123.456", "All Green Street", "Big Apple", "Blue Clound");
        
        // Partial update: only update street and city
        String bodyJson = """
            {
                "addressRequest": {
                    "street": "Updated Street",
                    "city": "Updated City"
                }
            }
            """;

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address/" + original.getId());
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PatchRequestExchange exchange = new PatchRequestExchange(request);
        Object result = exchange.call();

        assertNotNull(result);
        assertTrue(result instanceof AddressResponse);
        
        AddressResponse updated = (AddressResponse) result;
        // Id should remain the same
        assertEquals(original.getId(), updated.getId());
        // Updated fields should change
        assertEquals("Updated Street", updated.getStreet());
        assertEquals("Updated City", updated.getCity());
        // Unchanged fields should stay the same
        assertEquals(original.getZipcode(), updated.getZipcode());
        assertEquals(original.getState(), updated.getState());
    }

    @Test
    void patchRequestShouldReturnNullWhenResourceNotFound() throws Exception {
        String bodyJson = """
            {
                "addressRequest": {
                    "street": "Updated Street"
                }
            }
            """;

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address/99999");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(bodyJson)));
        when(request.getParameterMap()).thenReturn(Map.of());

        PatchRequestExchange exchange = new PatchRequestExchange(request);
        Object result = exchange.call();

        assertNull(result);
    }
}