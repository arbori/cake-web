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

class HeadRequestExchangeTest {
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
    void headRequestShouldReturnObjectWithoutBody() throws Exception {
        // HEAD should behave like GET but without response body
        AddressResponse expected = loanService.createAddress("123.456", "All Green Street", "Big Apple", "Blue Clound");
        
        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address/" + expected.getId());
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(Map.of());

        HeadRequestExchange exchange = new HeadRequestExchange(request);
        Object result = exchange.call();

        // HEAD should return the resource (same as GET)
        assertNotNull(result);
        assertTrue(result instanceof AddressResponse);
        
        AddressResponse retrieved = (AddressResponse) result;
        expected.setId(retrieved.getId());
        assertEquals(expected, retrieved);
    }

    @Test
    void headRequestShouldReturnNullWhenResourceNotFound() throws Exception {
        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address/99999");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(Map.of());

        HeadRequestExchange exchange = new HeadRequestExchange(request);
        Object result = exchange.call();

        assertNull(result);
    }
}