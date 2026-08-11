package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thebank.loan.model.AddressResponse;
import com.thebank.loan.service.LoanService;

class GetRequestExchangeTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    static LoanService loanService = new LoanService();

    List<AddressResponse> addresses;

    @BeforeAll
    static void beforeAll() {
        loanService.createAddress("123-456", "Rua dos Afogados, 23", "São Paulo", "São Paulo");
        loanService.createAddress("171-666", "Rua Alada, 17", "Rio de Janeiro", "Rio de Janeiro");
        loanService.createAddress("456-7686", "Avenida Alada, 1007", "Rio Prado", "Goias");
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        addresses = loanService.getAllAddresses();
    }

    //---------------------------------------------------------------------//
    @Test
    void getRequestExchangeRootPackage() throws IOException {
        String messageExpected = "The Bank Loan System v1.0";

        when(request.getRequestURI()).thenReturn("thebank.com/about");
        when(request.getContextPath()).thenReturn("thebank.com/");

        GetRequestExchange getRequestExchange = new GetRequestExchange(request);

        Object result = null;
        
        try {
            result = getRequestExchange.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(result instanceof String, "Result should be a String");
        assertEquals(messageExpected, result, "The return of get method is different than expected");
    }

    //---------------------------------------------------------------------//
    @Test
    void getRequestExchangePathId() throws IOException {
        AddressResponse responseExpected = addresses.get(0);

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address/" + responseExpected.getId());
        when(request.getContextPath()).thenReturn("thebank.com/");

        GetRequestExchange getRequestExchange = new GetRequestExchange(request);

        Object result = null;
        
        try {
            result = getRequestExchange.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNotNull(result, "Result must not be null");

        assertTrue(result instanceof AddressResponse, "Result should be a AddressResponse");

        AddressResponse address = (AddressResponse) result;

        assertEquals(responseExpected, address, "The return of get method is different than expected");
    }

    @Test
    void getRequestExchangeWithQueryParameters() throws IOException {
        AddressResponse responseExpected = addresses.get(0);

        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(
            Map.of(
                "street", new String[] { responseExpected.getStreet() }
            )
        );

        GetRequestExchange getRequestExchange = new GetRequestExchange(request);

        Object result = null;

        try {
            result = getRequestExchange.call();
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(result instanceof List<?>, "Result should be a List<AddressResponse>");
        
        AddressResponse address = (AddressResponse) ((List<?>) result).getFirst();

        assertEquals(responseExpected.getZipcode(), address.getZipcode(), "The zipcode is different");
        assertEquals(responseExpected.getStreet(), address.getStreet(), "The street is different");
        assertEquals(responseExpected.getCity(), address.getCity(), "The city is different");
        assertEquals(responseExpected.getState(), address.getState(), "The state is different");
    }
}
