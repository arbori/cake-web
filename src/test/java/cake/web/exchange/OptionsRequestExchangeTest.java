package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.thebank.loan.model.AddressResponse;
import com.thebank.loan.service.LoanService;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class OptionsRequestExchangeTest {
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
    void optionsRequestShouldReturnResource() throws Exception {
        AddressResponse expected = loanService.createAddress("123.456", "All Green Street", "Big Apple", "Blue Clound");
        
        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address/" + expected.getId());
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(Map.of());

        OptionsRequestExchange exchange = new OptionsRequestExchange(request);
        Object result = exchange.call();

        // OPTIONS returns the resource (same as GET)
        assertNotNull(result);
        assertTrue(result instanceof AddressResponse);
        
        AddressResponse retrieved = (AddressResponse) result;
        expected.setId(retrieved.getId());
        assertEquals(expected, retrieved);
    }

    @Test
    void optionsRequestShouldReturnNullWhenResourceNotFound() throws Exception {
        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address/99999");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(Map.of());

        OptionsRequestExchange exchange = new OptionsRequestExchange(request);
        Object result = exchange.call();

        assertNull(result);
    }
}