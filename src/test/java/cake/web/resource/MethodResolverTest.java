package cake.web.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cake.web.exception.PrimitiveNotAllowedException;
import cake.web.exchange.HttpDataHandle;
import cake.web.exchange.HttpMethodName;
import cake.web.exchange.content.BodyContent;
import cake.web.exchange.content.HeaderContent;
import cake.web.exchange.content.QueryParamContent;

class MethodResolverTest {

    // ==================== MOCKS ====================
    @Mock
    HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== TEST RESOURCE CLASSES ====================

    // Valid resource with different parameter counts (allowed)
    public static class ValidResource {
        public void get() { /* For testing */ }
        public void get(Long id) { /* For testing */ }
        public void get(Long id, String name) { /* For testing */ }
        public void post(String name, Integer age) { /* For testing */ }
        public void put(Integer id, String value) { /* For testing */ }
        public void delete(UUID uuid) { /* For testing */ }
        public void patch(LocalDate date) { /* For testing */ }
        public void options(LocalDateTime dateTime) { /* For testing */ }
        public void head(BigDecimal amount) { /* For testing */ }
    }

    // Overloaded resource with same parameter count (ambiguous - NOT allowed)
    public static class AmbiguousResource {
        public void get(Integer id) { /* For testing */ }
        public void get(String name) { /* For testing */ }
        public void get(Long id, Integer page) { /* For testing */ }
        public void get(String query, Integer page) { /* For testing */ }
        public void get(Integer id, String name) { /* For testing */ }
    }

    // Resource with primitive types (should be incompatible)
    public static class PrimitiveResource {
        public void get(int id) { /* For testing */ }
        public void post(long id, String name) { /* For testing */ }
    }

    // Resource with no default constructor
    public static class NoDefaultConstructorResource {
        public NoDefaultConstructorResource(String arg) { /* For testing */ }
        public void get() { /* For testing */ }
    }

    // Resource with private constructor
    public static class PrivateConstructorResource {
        private PrivateConstructorResource() { /* For testing */ }
        public void get() { /* For testing */ }
    }

    // Resource with static method
    public static class StaticMethodResource {
        public static void get() { /* For testing */ }
    }

    // Resource with private method
    public static class PrivateMethodResource {
        @SuppressWarnings("unused")
        private void get() { /* For testing */ }
    }

    // Resource with no methods
    public static class EmptyResource { /* For testing */ }

    // Resource with inherited methods
    public static class ParentResource {
        public void get() { /* For testing */ }
        public void get(Integer id) { /* For testing */ }
    }
    
    public static class ChildResource extends ParentResource {
        public void get(Long id) { /* For testing */ }
    }

    public static class NotOnlyPathBody implements BodyContent {
        public Integer id;
        public String name;
        public Long fiscalNumber;

        public Integer getId() {
            return id;
        }
        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }

        public Long getFiscalNumber() {
            return fiscalNumber;
        }
        public void setFiscalNumber(Long fiscalNumber) {
            this.fiscalNumber = fiscalNumber;
        }
    }

    public static class NotOnlyPathQuery implements QueryParamContent {
        public String name;

        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }

    public static class NotOnlyPathHeader implements HeaderContent {
        public String Authorization;

        public String getAuthorization() {
            return Authorization;
        }
        public void setAuthorization(String Authorization) {
            this.Authorization = Authorization;
        }
    }

    public static class NotOnlyPathResource {
        public void get(Integer id, NotOnlyPathQuery query) { 
            assertNotNull(id);
            assertNotNull(query);
            assertNotNull(query.getName());
        }
        public void post(Integer id, NotOnlyPathBody body) {
            assertNotNull(id);
            assertNotNull(body);
            assertNotNull(body.getId());
            assertNotNull(body.getName());
            assertNotNull(body.getFiscalNumber());
        }
        public void put(Integer id, NotOnlyPathBody body, NotOnlyPathHeader header) {
            assertNotNull(id);
            assertNotNull(body);
            assertNotNull(body.getId());
            assertNotNull(body.getName());
            assertNotNull(body.getFiscalNumber());
            assertNotNull(header);
            assertNotNull(header.getAuthorization());
        }
    }

    // ==================== SUCCESS CASES ====================

    @Test
    void shouldResolveMethodWithZeroParameters() throws Exception {
        List<Object> pathParams = List.of();
        
        Method method = MethodResolver.methodResolution(
            ValidResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(method);
        assertEquals("get", method.getName());
        assertEquals(0, method.getParameterCount());
    }

    @Test
    void shouldResolveMethodWithOneIntegerParameter() throws Exception {
        List<Object> pathParams = List.of("123");
        
        Method method = MethodResolver.methodResolution(
            ParentResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(method);
        assertEquals("get", method.getName());
        assertEquals(1, method.getParameterCount());
        assertEquals(Integer.class, method.getParameterTypes()[0]);
    }

    @Test
    void shouldResolveMethodWithTwoParameters() throws Exception {
        List<Object> pathParams = List.of("456", "John");
        
        Method method = MethodResolver.methodResolution(
            ValidResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(method);
        assertEquals("get", method.getName());
        assertEquals(2, method.getParameterCount());
        assertEquals(Long.class, method.getParameterTypes()[0]);
        assertEquals(String.class, method.getParameterTypes()[1]);
    }

    @Test
    void shouldResolveMethodWithUUIDParameter() throws Exception {
        UUID uuid = UUID.randomUUID();
        List<Object> pathParams = List.of(uuid.toString());
        
        Method mathod = MethodResolver.methodResolution(
            ValidResource.class, HttpMethodName.DELETE, pathParams
        );
        
        assertNotNull(mathod);
        assertEquals("delete", mathod.getName());
        assertEquals(UUID.class, mathod.getParameterTypes()[0]);
    }

    @Test
    void shouldResolveMethodWithLocalDateParameter() throws Exception {
        LocalDate date = LocalDate.of(2024, 5, 15);
        List<Object> pathParams = List.of(date.toString());
        
        Method method = MethodResolver.methodResolution(
            ValidResource.class, HttpMethodName.PATCH, pathParams
        );
        
        assertNotNull(method);
        assertEquals("patch", method.getName());
        assertEquals(LocalDate.class, method.getParameterTypes()[0]);
    }

    @Test
    void shouldResolveMethodWithLocalDateTimeParameter() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2024, 5, 15, 14, 30, 45);
        List<Object> pathParams = List.of(dateTime.toString());
        
        Method method = MethodResolver.methodResolution(
            ValidResource.class, HttpMethodName.OPTIONS, pathParams
        );
        
        assertNotNull(method);
        assertEquals("options", method.getName());
        assertEquals(LocalDateTime.class, method.getParameterTypes()[0]);
    }

    @Test
    void shouldResolveMethodWithBigDecimalParameter() throws Exception {
        BigDecimal amount = new BigDecimal("123.45");
        List<Object> pathParams = List.of(amount.toString());
        
        Method method = MethodResolver.methodResolution(
            ValidResource.class, HttpMethodName.HEAD, pathParams
        );
        
        assertNotNull(method);
        assertEquals("head", method.getName());
        assertEquals(BigDecimal.class, method.getParameterTypes()[0]);
    }

    // ==================== INHERITED METHODS ====================

    @Test
    void shouldResolveInheritedMethodFromParent() throws Exception {
        List<Object> pathParams = List.of();
        
        Method method = MethodResolver.methodResolution(
            ChildResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(method);
        assertEquals("get", method.getName());
        assertEquals(0, method.getParameterCount());
        assertEquals(ParentResource.class, method.getDeclaringClass());
    }

    @Test
    void shouldResolveOwnMethodBeforeInherited() throws Exception {
        List<Object> pathParams = List.of("456");
        
        Method method = MethodResolver.methodResolution(
            ChildResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(method);
        assertEquals("get", method.getName());
        assertEquals(1, method.getParameterCount());
        assertEquals(Long.class, method.getParameterTypes()[0]);
        assertEquals(ChildResource.class, method.getDeclaringClass());
    }

    // ==================== AMBIGUITY CASES (Same parameter count) ====================

    @Test
    void shouldThrowExceptionWhenMultipleMethodsWithSameParameterCount() {
        List<Object> pathParams = List.of("123");  // 1 parameter
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodResolver.methodResolution(AmbiguousResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("Ambiguity call"));
        assertTrue(exception.getMessage().contains("Endpoint overload is not allowed"));
        assertTrue(exception.getMessage().contains("get(Integer)"));
        assertTrue(exception.getMessage().contains("get(String)"));
    }

    @Test
    void shouldThrowExceptionWhenMultipleMethodsWithSameTwoParameterCount() {
        List<Object> pathParams = List.of("100", "1");  // 2 parameters
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodResolver.methodResolution(AmbiguousResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("Ambiguity call"));
        assertTrue(exception.getMessage().contains("Endpoint overload is not allowed"));
    }

    // ==================== NOT FOUND CASES ====================

    @Test
    void shouldThrowExceptionWhenNoMethodWithMatchingNameExists() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodResolver.methodResolution(ValidResource.class, HttpMethodName.PUT, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named"));
        assertTrue(exception.getMessage().contains("ValidResource.put"));
    }

    @Test
    void shouldThrowExceptionWhenMethodExistsButParameterCountMismatch() {
        List<Object> pathParams = List.of("123", "Say my name", "456");  // 2 params, but get(Integer) exists with 1
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodResolver.methodResolution(ValidResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named"));
        assertTrue(exception.getMessage().contains("ValidResource.get"));
    }

    @Test
    void shouldThrowExceptionWhenMethodExistsButParameterTypesIncompatible() {
        List<Object> pathParams = List.of("not a number");
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodResolver.methodResolution(ValidResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No method"));
        assertTrue(exception.getMessage().contains("compatible with path parameters"));
    }

    @Test
    void shouldThrowExceptionWhenResourceClassHasNoMethods() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodResolver.methodResolution(EmptyResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named"));
    }

    // ==================== PRIMITIVE TYPE HANDLING ====================

    @Test
    void shouldIgnoreMethodsWithPrimitiveParameters() {
        List<Object> pathParams = List.of("100");
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodResolver.methodResolution(PrimitiveResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No method cake.web.resource.TypeResolverTest$PrimitiveResource.get compatible with path parameters: [100]"));
    }

    // ==================== CONSTRUCTOR VALIDATION ====================

    @Test
    void shouldThrowExceptionWhenResourceClassHasNoDefaultConstructor() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodResolver.methodResolution(NoDefaultConstructorResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("constructor"));
    }

    @Test
    void shouldThrowExceptionWhenResourceClassConstructorIsPrivate() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodResolver.methodResolution(PrivateConstructorResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("constructor"));
    }

    // ==================== STATIC METHOD HANDLING ====================

    @Test
    void shouldIgnoreStaticMethods() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodResolver.methodResolution(StaticMethodResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named"));
    }

    // ==================== PRIVATE METHOD HANDLING ====================

    @Test
    void shouldIgnorePrivateMethods() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodResolver.methodResolution(PrivateMethodResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named"));
    }

    // ==================== NULL INPUT VALIDATION ====================

    @Test
    void shouldThrowExceptionWhenResourceClassIsNull() {
        List<Object> pathParams = List.of();
        
        assertThrows(IllegalArgumentException.class, () ->
            MethodResolver.methodResolution(null, HttpMethodName.GET, pathParams)
        );
    }

    @Test
    void shouldThrowExceptionWhenHttpMethodNameIsNull() {
        List<Object> pathParams = List.of();
        
        assertThrows(IllegalArgumentException.class, () ->
            MethodResolver.methodResolution(ValidResource.class, null, pathParams)
        );
    }

    @Test
    void shouldThrowExceptionWhenPathParamsIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            MethodResolver.methodResolution(ValidResource.class, HttpMethodName.GET, null)
        );
    }

    // ==================== EDGE CASES ====================

    @Test
    void shouldHandleEmptyPathParamsList() throws Exception {
        List<Object> pathParams = List.of();
        
        Method method = MethodResolver.methodResolution(
            ValidResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(method);
        assertEquals(0, method.getParameterCount());
    }

    @Test
    void shouldHandleLargeIntegerValue() throws Exception {
        List<Object> pathParams = List.of(Integer.toString(Integer.MAX_VALUE));
        
        Method method = MethodResolver.methodResolution(
            ParentResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(method);
        assertEquals(Integer.class, method.getParameterTypes()[0]);
    }

    @Test
    void shouldHandleLargeLongValue() throws Exception {
        List<Object> pathParams = List.of(Long.toString(Long.MAX_VALUE));
        
        Method method = MethodResolver.methodResolution(
            ValidResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(method);
        assertEquals(Long.class, method.getParameterTypes()[0]);
    }

    // ==================== POST METHOD TEST ====================

    @Test
    void shouldResolvePostMethod() throws Exception {
        List<Object> pathParams = List.of("John", "30");
        
        Method method = MethodResolver.methodResolution(
            ValidResource.class, HttpMethodName.POST, pathParams
        );
        
        assertNotNull(method);
        assertEquals("post", method.getName());
        assertEquals(String.class, method.getParameterTypes()[0]);
        assertEquals(Integer.class, method.getParameterTypes()[1]);
    }

    // ==================== convertPathParams TESTS ====================

    @Test
    void shouldConvertPathParams() throws IOException {
        List<Object> pathParams = List.of("123", "John", "true");
        Class<?>[] paramTypes = {Integer.class, String.class, Boolean.class};

        when(httpServletRequest.getParameterMap()).thenReturn(Map.of());
        when(httpServletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(httpServletRequest.getReader()).thenReturn(null);
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        HttpDataHandle httpDataHandle = new HttpDataHandle(httpServletRequest);

        List<Object> result = MethodResolver.convertPathParams(paramTypes, pathParams, httpDataHandle);
        
        assertEquals(3, result.size());
        assertEquals(123, result.get(0));
        assertEquals("John", result.get(1));
        assertTrue((Boolean) result.get(2));
    }

    @Test
    void shouldThrowExceptionWhenPathParamsSizeExceedsParamTypes() throws IOException {
        List<Object> pathParams = List.of("123", "456");
        Class<?>[] paramTypes = {Integer.class};

        when(httpServletRequest.getParameterMap()).thenReturn(Map.of());
        when(httpServletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(httpServletRequest.getReader()).thenReturn(null);
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        HttpDataHandle httpDataHandle = new HttpDataHandle(httpServletRequest);
        
        // convertPathParams iterates over pathParams.size()
        // but parameterTypes[i] will throw ArrayIndexOutOfBoundsException
        assertThrows(ArrayIndexOutOfBoundsException.class, 
            () -> MethodResolver.convertPathParams(paramTypes, pathParams, httpDataHandle));
    }

    @Test
    void shouldThrowExceptionWhenParamTypesIsPrimitive() throws IOException {
        List<Object> pathParams = List.of("123", "456");
        Class<?>[] paramTypes = {int.class, long.class};

        when(httpServletRequest.getParameterMap()).thenReturn(Map.of());
        when(httpServletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(httpServletRequest.getReader()).thenReturn(null);
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        HttpDataHandle httpDataHandle = new HttpDataHandle(httpServletRequest);
        
        // convertPathParams iterates over pathParams.size()
        // but parameterTypes[i] will throw ArrayIndexOutOfBoundsException
        assertThrows(PrimitiveNotAllowedException.class, 
            () -> MethodResolver.convertPathParams(paramTypes, pathParams, httpDataHandle));
    }

    @Test
    void filterQueryFromParameter() throws NoSuchMethodException, IOException {
        List<Object> pathParams = List.of("123");
        Map<String, String[]> queryParameter = Map.of("name", new String[] {"John"});

        when(httpServletRequest.getParameterMap()).thenReturn(queryParameter);
        when(httpServletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(httpServletRequest.getReader()).thenReturn(null);
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        HttpDataHandle httpDataHandle = new HttpDataHandle(httpServletRequest);

        Method method = MethodResolver.methodResolution(
            NotOnlyPathResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(method);
        assertEquals("get", method.getName());
        assertEquals(Integer.class, method.getParameterTypes()[0]);
        assertEquals(NotOnlyPathQuery.class, method.getParameterTypes()[1]);

        List<Object> methodParameterData = MethodResolver.convertPathParams(method.getParameterTypes(), pathParams, httpDataHandle);

        assertInstanceOf(Integer.class, methodParameterData.get(0));
        assertInstanceOf(NotOnlyPathQuery.class, methodParameterData.get(1));
        assertEquals(123, ((Integer) methodParameterData.get(0)));
        assertEquals("John", ((NotOnlyPathQuery) methodParameterData.get(1)).getName());

    }

    @Test
    void postWithIdAndBody() throws NoSuchMethodException, IOException {
        List<Object> pathParams = List.of("123");
        BufferedReader bodyContent = new BufferedReader(new StringReader( 
        """
        {
            \"notOnlyPathBody\": {
                \"name\": \"John\",
                \"fiscalNumber\": 4
            }
        }
        """));

        when(httpServletRequest.getParameterMap()).thenReturn(Collections.emptyMap());
        when(httpServletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(httpServletRequest.getReader()).thenReturn(bodyContent);
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        HttpDataHandle httpDataHandle = new HttpDataHandle(httpServletRequest);

        Method method = MethodResolver.methodResolution(
            NotOnlyPathResource.class, HttpMethodName.POST, pathParams
        );
        
        assertNotNull(method);
        assertEquals("post", method.getName());
        assertEquals(Integer.class, method.getParameterTypes()[0]);
        assertEquals(NotOnlyPathBody.class, method.getParameterTypes()[1]);

        List<Object> methodParameterData = MethodResolver.convertPathParams(method.getParameterTypes(), pathParams, httpDataHandle);

        assertInstanceOf(Integer.class, methodParameterData.get(0));
        assertInstanceOf(NotOnlyPathBody.class, methodParameterData.get(1));
        assertEquals(123, ((Integer) methodParameterData.get(0)));
        assertEquals(4, ((NotOnlyPathBody) methodParameterData.get(1)).getFiscalNumber());
        assertEquals("John", ((NotOnlyPathBody) methodParameterData.get(1)).getName());
    }

    @Test
    void putWithIdBodyHeader() throws NoSuchMethodException, IOException {
        List<Object> pathParams = List.of("123");
        BufferedReader bodyContent = new BufferedReader(new StringReader( 
        """
        {
            \"notOnlyPathBody\": {
                \"id\": 123,
                \"name\": \"John\",
                \"fiscalNumber\": 4
            }
        }
        """));

        String expectedToken = "Bearer eyJhbGciOiJIUzI1NiIsInBFYJBfUJffHuhFEedfBKIKgERYKhDYJjuRDFH";

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", expectedToken);

        // create an Enumeration over the header keys
        Enumeration<String> headerNames = Collections.enumeration(headers.keySet());

        when(httpServletRequest.getParameterMap()).thenReturn(Collections.emptyMap());
        when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);
        when(httpServletRequest.getHeader("Authorization")).thenReturn(expectedToken);
        when(httpServletRequest.getReader()).thenReturn(bodyContent);

        HttpDataHandle httpDataHandle = new HttpDataHandle(httpServletRequest);

        Method method = MethodResolver.methodResolution(
            NotOnlyPathResource.class, HttpMethodName.PUT, pathParams
        );
        
        assertNotNull(method);
        assertEquals("put", method.getName());
        assertEquals(Integer.class, method.getParameterTypes()[0]);
        assertEquals(NotOnlyPathBody.class, method.getParameterTypes()[1]);
        assertEquals(NotOnlyPathHeader.class, method.getParameterTypes()[2]);

        List<Object> methodParameterData = MethodResolver.convertPathParams(method.getParameterTypes(), pathParams, httpDataHandle);

        assertInstanceOf(Integer.class, methodParameterData.get(0));
        assertInstanceOf(NotOnlyPathBody.class, methodParameterData.get(1));
        assertInstanceOf(NotOnlyPathHeader.class, methodParameterData.get(2));
        assertEquals(123, ((Integer) methodParameterData.get(0)));
        assertEquals(123, ((NotOnlyPathBody) methodParameterData.get(1)).getId());
        assertEquals(4, ((NotOnlyPathBody) methodParameterData.get(1)).getFiscalNumber());
        assertEquals("John", ((NotOnlyPathBody) methodParameterData.get(1)).getName());
        assertEquals(expectedToken, ((NotOnlyPathHeader) methodParameterData.get(2)).getAuthorization());
    }
}
