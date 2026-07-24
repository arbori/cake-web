package cake.web.exchange;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cake.web.exchange.content.BodyContent;
import cake.web.exchange.content.HeaderContent;
import cake.web.exchange.content.QueryParamContent;

import javax.servlet.http.HttpServletRequest;

class HttpDataHandleTest {

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== TEST CLASSES ====================

    public static class TestBody implements BodyContent {
        private Integer id;
        private String name;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class TestQuery implements QueryParamContent {
        private String city;
        private Integer age;

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
    }

    public static class TestHeader implements HeaderContent {
        private String authorization;
        private String xRequestId;

        public String getAuthorization() { return authorization; }
        public void setAuthorization(String authorization) { this.authorization = authorization; }
        public String getXRequestId() { return xRequestId; }
        public void setXRequestId(String xRequestId) { this.xRequestId = xRequestId; }
    }

    public static class TestHeaderWithSetter implements HeaderContent {
        private String authorization;
        private String xRequestId;

        public String getAuthorization() { return authorization; }
        public void setAuthorization(String authorization) { this.authorization = authorization; }
        public String getXRequestId() { return xRequestId; }
        public void setXRequestId(String xRequestId) { this.xRequestId = xRequestId; }
    }

    // ==================== BUILD FROM BODY TESTS ====================

    @Test
    void shouldBuildFromBody() throws Exception {
        String json = "{\"testBody\": {\"id\": 123, \"name\": \"John Doe\"}}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

        HttpDataHandle handle = new HttpDataHandle(request);
        TestBody result = (TestBody) handle.buildFromBody(TestBody.class);

        assertNotNull(result);
        assertEquals(123, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void shouldReturnNullWhenBodyIsNull() throws Exception {
        when(request.getReader()).thenReturn(null);
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

        HttpDataHandle handle = new HttpDataHandle(request);
        TestBody result = (TestBody) handle.buildFromBody(TestBody.class);

        assertNull(result);
    }

    @Test
    void shouldThrowExceptionWhenBodyHasWrongKey() throws Exception {
        String json = "{\"wrongKey\": {\"id\": 123}}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

        HttpDataHandle handle = new HttpDataHandle(request);

        assertThrows(IllegalArgumentException.class, () ->
            handle.buildFromBody(TestBody.class)
        );
    }

    @Test
    void shouldThrowExceptionWhenBodyIsMalformed() throws Exception {
        String json = "{not valid json}";
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());

        HttpDataHandle handle = new HttpDataHandle(request);

        assertThrows(IllegalArgumentException.class, () ->
            handle.buildFromBody(TestBody.class)
        );
    }

    // ==================== BUILD FROM QUERY TESTS ====================

    @Test
    void shouldBuildFromQueryParameters() throws Exception {
        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("city", new String[]{"São Paulo"});
        queryParams.put("age", new String[]{"25"});

        when(request.getParameterMap()).thenReturn(queryParams);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getReader()).thenReturn(null);

        HttpDataHandle handle = new HttpDataHandle(request);
        TestQuery result = (TestQuery) handle.buildFromQueryParameter(TestQuery.class);

        assertNotNull(result);
        assertEquals("São Paulo", result.getCity());
        assertEquals(25, result.getAge());
    }

    @Test
    void shouldHandleMissingQueryParameters() throws Exception {
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getReader()).thenReturn(null);

        HttpDataHandle handle = new HttpDataHandle(request);
        TestQuery result = (TestQuery) handle.buildFromQueryParameter(TestQuery.class);

        assertNotNull(result);
        assertNull(result.getCity());
        assertNull(result.getAge());
    }

    @Test
    void shouldHandleQueryParameterWithMultipleValues() throws Exception {
        Map<String, String[]> queryParams = new HashMap<>();
        queryParams.put("city", new String[]{"São Paulo", "Rio"});

        when(request.getParameterMap()).thenReturn(queryParams);
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getReader()).thenReturn(null);

        HttpDataHandle handle = new HttpDataHandle(request);
        TestQuery result = (TestQuery) handle.buildFromQueryParameter(TestQuery.class);

        assertNotNull(result);
        assertEquals("São Paulo", result.getCity()); // Only first value used
    }

    // ==================== BUILD FROM HEADER TESTS ====================

    @Test
    void shouldBuildFromHeaders() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer token123");
        headers.put("x-request-id", "req-456");

        Enumeration<String> headerNames = Collections.enumeration(headers.keySet());

        when(request.getHeaderNames()).thenReturn(headerNames);
        when(request.getHeader("authorization")).thenReturn("Bearer token123");
        when(request.getHeader("x-request-id")).thenReturn("req-456");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getReader()).thenReturn(null);

        HttpDataHandle handle = new HttpDataHandle(request);
        TestHeader result = (TestHeader) handle.buildFromHeader(TestHeader.class);

        assertNotNull(result);
        assertEquals("Bearer token123", result.getAuthorization());
        assertEquals("req-456", result.getXRequestId());
    }

    @Test
    void shouldHandleMissingHeaders() throws Exception {
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getReader()).thenReturn(null);

        HttpDataHandle handle = new HttpDataHandle(request);
        TestHeader result = (TestHeader) handle.buildFromHeader(TestHeader.class);

        assertNotNull(result);
        assertNull(result.getAuthorization());
        assertNull(result.getXRequestId());
    }

    @Test
    void shouldHandleHeaderWithDifferentCase() throws Exception {
        // Headers are case-insensitive, but HttpDataHandle should handle both
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer token123");

        Enumeration<String> headerNames = Collections.enumeration(headers.keySet());

        when(request.getHeaderNames()).thenReturn(headerNames);
        when(request.getHeader("Authorization")).thenReturn("Bearer token123");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getReader()).thenReturn(null);

        HttpDataHandle handle = new HttpDataHandle(request);
        TestHeader result = (TestHeader) handle.buildFromHeader(TestHeader.class);

        assertNotNull(result);
        assertEquals("Bearer token123", result.getAuthorization());
    }

    @Test
    void shouldUseSetterIfAvailable() throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("authorization", "Bearer token123");

        Enumeration<String> headerNames = Collections.enumeration(headers.keySet());

        when(request.getHeaderNames()).thenReturn(headerNames);
        when(request.getHeader("authorization")).thenReturn("Bearer token123");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getReader()).thenReturn(null);

        HttpDataHandle handle = new HttpDataHandle(request);
        TestHeaderWithSetter result = (TestHeaderWithSetter) handle.buildFromHeader(TestHeaderWithSetter.class);

        assertNotNull(result);
        assertEquals("Bearer token123", result.getAuthorization());
    }

    // ==================== AUTH TOKEN TESTS ====================

    @Test
    void shouldExtractBearerToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer abc123xyz");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getReader()).thenReturn(null);

        HttpDataHandle handle = new HttpDataHandle(request);

        assertEquals("abc123xyz", handle.getAuthToken());
    }

    @Test
    void shouldReturnNullWhenNoAuthHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getReader()).thenReturn(null);

        HttpDataHandle handle = new HttpDataHandle(request);

        assertNull(handle.getAuthToken());
    }

    @Test
    void shouldReturnFullHeaderWhenNotBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());
        when(request.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(request.getReader()).thenReturn(null);

        HttpDataHandle handle = new HttpDataHandle(request);

        assertEquals("Basic dXNlcjpwYXNz", handle.getAuthToken());
    }
}