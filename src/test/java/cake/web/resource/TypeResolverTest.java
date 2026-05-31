package cake.web.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import cake.web.exchange.HttpMethodName;

class TypeResolverTest {

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

    // ==================== SUCCESS CASES ====================

    @Test
    void shouldResolveMethodWithZeroParameters() throws Exception {
        List<Object> pathParams = List.of();
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ValidResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(0, resolution.method().getParameterCount());
        assertTrue(resolution.args().isEmpty());
    }

    @Test
    void shouldResolveMethodWithOneIntegerParameter() throws Exception {
        List<Object> pathParams = List.of("123");
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ParentResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(1, resolution.method().getParameterCount());
        assertEquals(Integer.class, resolution.method().getParameterTypes()[0]);
        assertEquals(123, resolution.args().get(0));
    }

    @Test
    void shouldResolveMethodWithTwoParameters() throws Exception {
        List<Object> pathParams = List.of("456", "John");
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ValidResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(2, resolution.method().getParameterCount());
        assertEquals(Long.class, resolution.method().getParameterTypes()[0]);
        assertEquals(String.class, resolution.method().getParameterTypes()[1]);
        assertEquals(456L, resolution.args().get(0));
        assertEquals("John", resolution.args().get(1));
    }

    @Test
    void shouldResolveMethodWithUUIDParameter() throws Exception {
        UUID uuid = UUID.randomUUID();
        List<Object> pathParams = List.of(uuid.toString());
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ValidResource.class, HttpMethodName.DELETE, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals("delete", resolution.method().getName());
        assertEquals(UUID.class, resolution.method().getParameterTypes()[0]);
        assertEquals(uuid, resolution.args().get(0));
    }

    @Test
    void shouldResolveMethodWithLocalDateParameter() throws Exception {
        LocalDate date = LocalDate.of(2024, 5, 15);
        List<Object> pathParams = List.of(date.toString());
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ValidResource.class, HttpMethodName.PATCH, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals("patch", resolution.method().getName());
        assertEquals(LocalDate.class, resolution.method().getParameterTypes()[0]);
        assertEquals(date, resolution.args().get(0));
    }

    @Test
    void shouldResolveMethodWithLocalDateTimeParameter() throws Exception {
        LocalDateTime dateTime = LocalDateTime.of(2024, 5, 15, 14, 30, 45);
        List<Object> pathParams = List.of(dateTime.toString());
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ValidResource.class, HttpMethodName.OPTIONS, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals("options", resolution.method().getName());
        assertEquals(LocalDateTime.class, resolution.method().getParameterTypes()[0]);
        assertEquals(dateTime, resolution.args().get(0));
    }

    @Test
    void shouldResolveMethodWithBigDecimalParameter() throws Exception {
        BigDecimal amount = new BigDecimal("123.45");
        List<Object> pathParams = List.of(amount.toString());
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ValidResource.class, HttpMethodName.HEAD, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals("head", resolution.method().getName());
        assertEquals(BigDecimal.class, resolution.method().getParameterTypes()[0]);
        assertEquals(amount, resolution.args().get(0));
    }

    // ==================== INHERITED METHODS ====================

    @Test
    void shouldResolveInheritedMethodFromParent() throws Exception {
        List<Object> pathParams = List.of();
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ChildResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(0, resolution.method().getParameterCount());
        assertEquals(ParentResource.class, resolution.method().getDeclaringClass());
    }

    @Test
    void shouldResolveOwnMethodBeforeInherited() throws Exception {
        List<Object> pathParams = List.of("456");
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ChildResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(1, resolution.method().getParameterCount());
        assertEquals(Long.class, resolution.method().getParameterTypes()[0]);
        assertEquals(ChildResource.class, resolution.method().getDeclaringClass());
    }

    // ==================== AMBIGUITY CASES (Same parameter count) ====================

    @Test
    void shouldThrowExceptionWhenMultipleMethodsWithSameParameterCount() {
        List<Object> pathParams = List.of("123");  // 1 parameter
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(AmbiguousResource.class, HttpMethodName.GET, pathParams)
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
            TypeResolver.methodResolution(AmbiguousResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("Ambiguity call"));
        assertTrue(exception.getMessage().contains("Endpoint overload is not allowed"));
    }

    // ==================== NOT FOUND CASES ====================

    @Test
    void shouldThrowExceptionWhenNoMethodWithMatchingNameExists() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(ValidResource.class, HttpMethodName.PUT, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named"));
        assertTrue(exception.getMessage().contains("ValidResource.put"));
    }

    @Test
    void shouldThrowExceptionWhenMethodExistsButParameterCountMismatch() {
        List<Object> pathParams = List.of("123", "Say my name", "456");  // 2 params, but get(Integer) exists with 1
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(ValidResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named"));
        assertTrue(exception.getMessage().contains("ValidResource.get"));
    }

    @Test
    void shouldThrowExceptionWhenMethodExistsButParameterTypesIncompatible() {
        List<Object> pathParams = List.of("not a number");
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(ValidResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No method"));
        assertTrue(exception.getMessage().contains("compatible with path parameters"));
    }

    @Test
    void shouldThrowExceptionWhenResourceClassHasNoMethods() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(EmptyResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named"));
    }

    // ==================== PRIMITIVE TYPE HANDLING ====================

    @Test
    void shouldIgnoreMethodsWithPrimitiveParameters() {
        List<Object> pathParams = List.of("100");
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(PrimitiveResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No method cake.web.resource.TypeResolverTest$PrimitiveResource.get compatible with path parameters: [100]"));
    }

    // ==================== CONSTRUCTOR VALIDATION ====================

    @Test
    void shouldThrowExceptionWhenResourceClassHasNoDefaultConstructor() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(NoDefaultConstructorResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("constructor"));
    }

    @Test
    void shouldThrowExceptionWhenResourceClassConstructorIsPrivate() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(PrivateConstructorResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("constructor"));
    }

    // ==================== STATIC METHOD HANDLING ====================

    @Test
    void shouldIgnoreStaticMethods() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(StaticMethodResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named"));
    }

    // ==================== PRIVATE METHOD HANDLING ====================

    @Test
    void shouldIgnorePrivateMethods() {
        List<Object> pathParams = List.of();
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(PrivateMethodResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named"));
    }

    // ==================== NULL INPUT VALIDATION ====================

    @Test
    void shouldThrowExceptionWhenResourceClassIsNull() {
        List<Object> pathParams = List.of();
        
        assertThrows(IllegalArgumentException.class, () ->
            TypeResolver.methodResolution(null, HttpMethodName.GET, pathParams)
        );
    }

    @Test
    void shouldThrowExceptionWhenHttpMethodNameIsNull() {
        List<Object> pathParams = List.of();
        
        assertThrows(IllegalArgumentException.class, () ->
            TypeResolver.methodResolution(ValidResource.class, null, pathParams)
        );
    }

    @Test
    void shouldThrowExceptionWhenPathParamsIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            TypeResolver.methodResolution(ValidResource.class, HttpMethodName.GET, null)
        );
    }

    // ==================== EDGE CASES ====================

    @Test
    void shouldHandleEmptyPathParamsList() throws Exception {
        List<Object> pathParams = List.of();
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ValidResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals(0, resolution.method().getParameterCount());
    }

    @Test
    void shouldHandleLargeIntegerValue() throws Exception {
        List<Object> pathParams = List.of(Integer.toString(Integer.MAX_VALUE));
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ParentResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals(Integer.class, resolution.method().getParameterTypes()[0]);
        assertEquals(Integer.MAX_VALUE, resolution.args().get(0));
    }

    @Test
    void shouldHandleLargeLongValue() throws Exception {
        List<Object> pathParams = List.of(Long.toString(Long.MAX_VALUE));
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ValidResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals(Long.class, resolution.method().getParameterTypes()[0]);
        assertEquals(Long.MAX_VALUE, resolution.args().get(0));
    }

    // ==================== POST METHOD TEST ====================

    @Test
    void shouldResolvePostMethod() throws Exception {
        List<Object> pathParams = List.of("John", "30");
        
        MethodResolution resolution = TypeResolver.methodResolution(
            ValidResource.class, HttpMethodName.POST, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals("post", resolution.method().getName());
        assertEquals(String.class, resolution.method().getParameterTypes()[0]);
        assertEquals(Integer.class, resolution.method().getParameterTypes()[1]);
    }
}
