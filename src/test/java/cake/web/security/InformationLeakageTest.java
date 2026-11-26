package cake.web.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cake.web.exception.AmbiguityException;
import cake.web.exception.BadRequestException;
import cake.web.exception.FrameworkException;
import cake.web.exception.MethodInvocationException;
import cake.web.exception.ResourceNotFoundException;
import cake.web.exception.ResourceExchangeException;
import cake.web.exception.mapper.DefaultExceptionMapper;
import cake.web.exchange.GetRequestExchange;

/**
 * <p>Security tests verifying that internal information does not leak to clients
 * through HTTP responses.</p>
 * 
 * <p>Covers CWE-209 (Information Exposure Through an Error Message) and
 * CWE-470 (Use of Externally-Controlled Input to Select Classes or Code).</p>
 */
class InformationLeakageTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private DefaultExceptionMapper mapper;
    private StringWriter responseBody;
    private PrintWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mapper = new DefaultExceptionMapper();
        responseBody = new StringWriter();
        responseWriter = new PrintWriter(responseBody);

        when(response.getWriter()).thenReturn(responseWriter);
    }

    // ==================== CWE-209: Information Exposure in Error Messages ====================

    @Test
    void shouldNotLeakClassNameWhenMethodInvocationFails() {
        // Arrange: Exception with internal class name in message
        MethodInvocationException ex = new MethodInvocationException(
            "Method com.bank.internal.SecretService.doSomething was not found.",
            new NoSuchMethodException("com.bank.internal.SecretService.doSomething")
        );

        // Act
        mapper.handle(ex, response);

        // Assert: Response does not contain internal class names
        responseWriter.flush();
        String body = responseBody.toString();

        assertFalse(body.contains("com.bank.internal"), 
            "Response must not leak internal package name");
        assertFalse(body.contains("SecretService"), 
            "Response must not leak internal class name");
        assertFalse(body.contains("doSomething"), 
            "Response must not leak internal method name");
        assertEquals("Internal server error", body.trim());
    }

    @Test
    void shouldNotLeakStackTraceWhenFrameworkExceptionOccurs() {
        // Arrange: FrameworkException with a nested cause containing file paths
        FrameworkException ex = new FrameworkException(
            "Failed to load class com.bank.loan.CustomerResource from /home/user/project/target/classes",
            new ClassNotFoundException("com.bank.loan.CustomerResource")
        );

        // Act
        mapper.handle(ex, response);

        // Assert
        responseWriter.flush();
        String body = responseBody.toString();

        assertFalse(body.contains("/home/user"), 
            "Response must not leak file system paths");
        assertFalse(body.contains("CustomerResource"), 
            "Response must not leak class names");
        assertFalse(body.contains("ClassNotFoundException"), 
            "Response must not leak exception class names");
        assertEquals("Internal server error", body.trim());
    }

    @Test
    void shouldNotLeakResourceNameWhenResourceNotFound() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException(
            "URI /com/bank/loan/internal/AdminResource does not match any resource"
        );

        // Act
        mapper.handle(ex, response);

        // Assert
        responseWriter.flush();
        String body = responseBody.toString();

        assertFalse(body.contains("AdminResource"), 
            "Response must not leak resource class name");
        assertFalse(body.contains("/com/bank/loan/internal"), 
            "Response must not leak internal URI paths");
        assertEquals("Resource not found", body.trim());
    }

    @Test
    void shouldNotLeakAmbiguityDetails() {
        // Arrange: AmbiguityException with method signatures
        AmbiguityException ex = new AmbiguityException(
            "Ambiguity call to com.bank.loan.Customer.get. " +
            "Endpoint overload is not allowed.\n" +
            "get(Integer)\nget(String)"
        );

        // Act
        mapper.handle(ex, response);

        // Assert
        responseWriter.flush();
        String body = responseBody.toString();

        assertFalse(body.contains("get(Integer)"), 
            "Response must not leak method signatures");
        assertFalse(body.contains("get(String)"), 
            "Response must not leak method signatures");
        assertFalse(body.contains("Customer"), 
            "Response must not leak class names");
        assertEquals("Internal server error", body.trim());
    }

    @Test
    void shouldNotLeakResourceExchangeDetails() {
        // Arrange
        ResourceExchangeException ex = new ResourceExchangeException(
            "Inconsistent URI: /com/bank/loan/customer/123/proposal"
        );

        // Act
        mapper.handle(ex, response);

        // Assert
        responseWriter.flush();
        String body = responseBody.toString();

        assertFalse(body.contains("/com/bank/loan"), 
            "Response must not leak internal URI");
        assertEquals("Internal server error", body.trim());
    }

    @Test
    void shouldNotLeakUnknownExceptionDetails() {
        // Arrange: An unexpected exception with sensitive info
        RuntimeException ex = new RuntimeException(
            "Database connection failed: jdbc:mysql://internal-db:3306/prod"
        );

        // Act
        mapper.handle(ex, response);

        // Assert
        responseWriter.flush();
        String body = responseBody.toString();

        assertFalse(body.contains("jdbc:mysql"), 
            "Response must not leak JDBC URLs");
        assertFalse(body.contains("internal-db"), 
            "Response must not leak internal hostnames");
        assertFalse(body.contains("3306"), 
            "Response must not leak ports");
        assertEquals("Unexpected error", body.trim());
    }

    @Test
    void shouldAllowBusinessExceptionMessagesToReachClient() {
        // Arrange: Business exceptions intentionally expose safe messages
        BadRequestException ex = new BadRequestException(
            "Customer name is required"
        );

        // Act
        mapper.handle(ex, response);

        // Assert
        responseWriter.flush();
        String body = responseBody.toString();

        assertEquals("Customer name is required", body.trim());
        verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    void shouldVerifyStatusCodeForResourceNotFound() {
        // Arrange
        ResourceNotFoundException ex = new ResourceNotFoundException("Internal detail");

        // Act
        mapper.handle(ex, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void shouldVerifyStatusCodeForFrameworkException() {
        // Arrange
        FrameworkException ex = new FrameworkException("Internal detail");

        // Act
        mapper.handle(ex, response);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    // ==================== CWE-470: Unsafe Reflection ====================

    @Test
    void shouldNotLoadClassOutsideBasePackage() throws Exception {
        // Note: this test depends on CakeWebRegistry being initialized with a base package
        // Arrange
        when(request.getRequestURI()).thenReturn("thebank.com/com/attacker/EvilResource/1");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act + Assert: The framework should not load classes outside the base package
        // and should not invoke them
        try {
            GetRequestExchange exchange = new GetRequestExchange(request);
            Object result = exchange.call();

            // If we reach here, verify no attacker class was invoked
            assertNull(result, "No resource should be resolved for out-of-package URI");
        } catch (Exception e) {
            // Expected — the framework rejects the resource
            assertTrue(e instanceof ResourceNotFoundException 
                    || e instanceof ResourceExchangeException
                    || e instanceof MethodInvocationException,
                "Exception should be a framework exception, not a reflection failure: " + e.getClass());
        }
    }

    @Test
    void shouldRejectClassNamesWithPathTraversal() throws Exception {
        // Arrange: URI with path traversal attempt
        when(request.getRequestURI()).thenReturn("thebank.com/../../../etc/passwd");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act + Assert
        try {
            GetRequestExchange exchange = new GetRequestExchange(request);
            exchange.call();
            fail("Expected an exception for path traversal URI");
        } catch (Exception e) {
            // Expected — path traversal must not resolve to a valid resource
            assertTrue(e instanceof ResourceNotFoundException 
                    || e instanceof ResourceExchangeException,
                "Path traversal should fail with a framework exception");
        }
    }

    @Test
    void shouldRejectJavaLangClasses() throws Exception {
        // Arrange: URI attempting to load java.lang classes
        when(request.getRequestURI()).thenReturn("thebank.com/java/lang/System");
        when(request.getContextPath()).thenReturn("thebank.com/");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("")));
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act + Assert
        try {
            GetRequestExchange exchange = new GetRequestExchange(request);
            Object result = exchange.call();
            assertNull(result, "java.lang.System should not be resolvable as a resource");
        } catch (Exception e) {
            assertTrue(e instanceof ResourceNotFoundException 
                    || e instanceof ResourceExchangeException,
                "java.lang classes should be rejected");
        }
    }
}