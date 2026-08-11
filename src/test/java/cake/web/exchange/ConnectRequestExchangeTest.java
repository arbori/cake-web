package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

class ConnectRequestExchangeTest {
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void connectRequestShouldHandleTunnel() throws Exception {
        // CONNECT is typically used for SSL tunnels/proxies
        // In the context of this framework, it should behave like GET
        when(request.getRequestURI()).thenReturn("thebank.com/loan/capture/address/1");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(Map.of());

        ConnectRequestExchange exchange = new ConnectRequestExchange(request);
        
        // Should not throw
        assertDoesNotThrow(() -> exchange.call());
    }
}