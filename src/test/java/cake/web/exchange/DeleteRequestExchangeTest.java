package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.thebank.loan.model.AddressResponse;
import com.thebank.loan.service.LoanService;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class DeleteRequestExchangeTest {
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
    void updateAddressTest() throws Exception {
        AddressResponse expected = loanService.createAddress("123.456", "All Green Street", "Big Apple", "Blue Clound");
        
        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address/" + expected.getId());
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(Map.of());

        GetRequestExchange getExchange = new GetRequestExchange(request);
        DeleteRequestExchange deleteExchange = new DeleteRequestExchange(request);

        // Try to delete the resource
        Object result = deleteExchange.call();

        assertTrue(result instanceof Optional<?>, "Result should be an Optional<?>");

        Optional<?> resultOptional = (Optional<?>) result;
        
        assertTrue(!resultOptional.isEmpty(), "Retrieve the expected address was not possible");

        AddressResponse retrieved = (AddressResponse) resultOptional.get();

        assertEquals(expected, retrieved);

        // Check if the resource is no more in the repository
        result = getExchange.call();

        assertEquals(null, result, "The address resource still there");
    }
}
