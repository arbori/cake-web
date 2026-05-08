package cake.web.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import cake.web.exchange.HttpMethodName;

class TypeResolverTest {
    // ==================== TEST RESOURCE CLASSES ====================

    public static class SimpleResource {
        public void get() { /* only test */ }
        public void get(Integer id) { /* only test */ }
        public void get(String name) { /* only test */ }
        public void post(String name, Integer age) { /* only test */ }
        public void put(Integer id, String value) { /* only test */ }
        public void delete(UUID uuid) { /* only test */ }
    }
    
    public static class OverloadedResource {
        public void options(Integer id) { /* only test */ }
        public void options(Long id) { /* only test */ }
        public void options(String name) { /* only test */ }
        public void options(Long id, Integer page) { /* only test */ }
        public void options(Short query, Integer page) { /* only test */ }
    }

    public static class MixedTypesResource {
        public void post(Integer id, String name) { /* only test */ }
        public void post(Long id, String name) { /* only test */ }
        public void post(String name, Integer id) { /* only test */ }
        public void post(Integer id, Integer age) { /* only test */ }
        public void post(String firstName, String lastName) { /* only test */ }
    }

    public static class NoDefaultConstructorResource {
        public NoDefaultConstructorResource(String arg) { }
        public void get() { /* only test */ }
    }

    public static class EmptyResource {
        // No methods
    }

    static class NonPublicConstructorResource {
        private NonPublicConstructorResource() { }
        public void get() { /* only test */ }
    }

    public static class StaticResourceMethod {
        public static void get() { /* only test */ }
    }


    // ==================== SUCCESS CASES ====================

    @Test
    void shouldResolveMethodWithZeroParameters() throws Exception {
        // Arrange
        List<Object> pathParams = List.of();
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, HttpMethodName.GET, pathParams
        );
        
        // Assert
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(0, resolution.method().getParameterCount());
        assertTrue(resolution.args().isEmpty());
    }

    @Test
    void shouldResolveMethodWithOneIntegerParameter() throws Exception {
        // Arrange
        List<Object> pathParams = List.of(123);
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, HttpMethodName.GET, pathParams
        );
        
        // Assert
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(1, resolution.method().getParameterCount());
        assertEquals(Integer.class, resolution.method().getParameterTypes()[0]);
        assertEquals(123, resolution.args().get(0));
    }

    @Test
    void shouldResolveMethodWithOneStringParameter() throws Exception {
        // Arrange
        List<Object> pathParams = List.of("john");
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, HttpMethodName.GET, pathParams
        );
        
        // Assert
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(1, resolution.method().getParameterCount());
        assertEquals(String.class, resolution.method().getParameterTypes()[0]);
        assertEquals("john", resolution.args().get(0));
    }

    @Test
    void shouldResolveMethodWithMultipleParameters() throws Exception {
        // Arrange
        List<Object> pathParams = List.of("john", 25);
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, HttpMethodName.POST, pathParams
        );
        
        // Assert
        assertNotNull(resolution);
        assertEquals("post", resolution.method().getName());
        assertEquals(2, resolution.method().getParameterCount());
        assertEquals(String.class, resolution.method().getParameterTypes()[0]);
        assertEquals(Integer.class, resolution.method().getParameterTypes()[1]);
        assertEquals("john", resolution.args().get(0));
        assertEquals(25, resolution.args().get(1));
    }

    @Test
    void shouldResolveMethodWithUUIDParameter() throws Exception {
        // Arrange
        String uuidString = "550e8400-e29b-41d4-a716-446655440000";
        List<Object> pathParams = List.of(uuidString);
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, HttpMethodName.DELETE, pathParams
        );
        
        // Assert
        assertNotNull(resolution);
        assertEquals("delete", resolution.method().getName());
        assertEquals(UUID.class, resolution.method().getParameterTypes()[0]);
        assertEquals(UUID.fromString(uuidString), resolution.args().get(0));
    }

    // ==================== AMBIGUITY CASES ====================

    @Test
    void shouldThrowExceptionWhenMultipleMethodsMatch() {
        // Arrange
        List<Object> pathParams = List.of(123);
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(OverloadedResource.class, HttpMethodName.OPTIONS, pathParams)
        );
        
        assertTrue(exception.getMessage().toLowerCase().contains("ambiguous"));
    }

    @Test
    void shouldThrowExceptionWhenMultipleMethodsMatchWithMultipleParameters() {
        // Arrange
        List<Object> pathParams = List.of(123, 1);
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(OverloadedResource.class, HttpMethodName.OPTIONS, pathParams)
        );
        
        assertTrue(exception.getMessage().toLowerCase().contains("ambiguous"));
    }

    // ==================== NOT FOUND CASES ====================

    @Test
    void shouldThrowExceptionWhenNoMethodWithMatchingNameExists() {
        // Arrange
        List<Object> pathParams = List.of();
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(EmptyResource.class, HttpMethodName.HEAD, pathParams)
        );
        
        assertTrue(exception.getMessage().toLowerCase().contains("no public non-static method named"));
    }

    @Test
    void shouldThrowExceptionWhenNoMethodWithCompatibleParametersExists() {
        // Arrange
        List<Object> pathParams = List.of("123", "456", "789"); // 3 params, but no method with 3 params
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(SimpleResource.class, HttpMethodName.GET, pathParams)
        );
        
        assertTrue(exception.getMessage().toLowerCase().contains("no method named"));
    }

    @Test
    void shouldThrowExceptionWhenParameterTypesDoNotMatch() {
        // Arrange
        List<Object> pathParams = List.of("not a number", "also not a number");
        
        // Act & Assert - SimpleResource has get(Integer) but value is not a number
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(OverloadedResource.class, HttpMethodName.OPTIONS, pathParams)
        );
        
        assertTrue(exception.getMessage().toLowerCase().contains("no method named"));
    }

    // ==================== NULL AND EDGE CASES ====================

    @Test
    void shouldThrowExceptionWhenResourceClassIsNull() {
        List<Object> pathParams = List.of();
        
        assertThrows(IllegalArgumentException.class, () ->
            TypeResolver.methodResolution(null, HttpMethodName.GET, pathParams)
        );
    }

    @Test
    void shouldThrowExceptionWhenMethodNameIsNull() {
        List<Object> pathParams = List.of();
        
        assertThrows(IllegalArgumentException.class, () ->
            TypeResolver.methodResolution(SimpleResource.class, null, pathParams)
        );
    }

    @Test
    void shouldThrowExceptionWhenPathParamsIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            TypeResolver.methodResolution(SimpleResource.class, HttpMethodName.GET, null)
        );
    }

    @Test
    void shouldHandleEmptyPathParams() throws Exception {
        // Arrange
        List<Object> pathParams = List.of();
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, HttpMethodName.GET, pathParams
        );
        
        // Assert
        assertNotNull(resolution);
        assertEquals(0, resolution.method().getParameterCount());
    }

    // ==================== TYPE SPECIFICITY CASES ====================

    @Test
    void shouldMatchStringWhenNumericConversionFails() throws Exception {
        // SimpleResource has: get(String) and get(Integer)
        // "abc" cannot be Integer, so should match get(String)
        
        List<Object> pathParams = List.of("abc");
        
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, HttpMethodName.GET, pathParams
        );
        
        assertNotNull(resolution);
        assertEquals(String.class, resolution.method().getParameterTypes()[0]);
        assertEquals("abc", resolution.args().get(0));
    }

    @Test
    void shouldResolveWhenMethodHasMixedParameterTypes() {
        // MixedTypesResource has multiple post methods
        // post(Integer id, String name) and post(String name, Integer id)
        // With pathParams ["123", "john"], both are compatible
        // Should be ambiguous
        
        List<Object> pathParams = List.of(123, "john");
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(MixedTypesResource.class, HttpMethodName.POST, pathParams)
        );
        
        assertTrue(exception.getMessage().toLowerCase().contains("ambiguous"));
    }

    @Test
    void shouldThrowExceptionWhenResourceClassHasNoPublicNoArgConstructor() {
        List<Object> pathParams = List.of();    
     
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(NoDefaultConstructorResource.class, HttpMethodName.GET, pathParams)
        );
     
        assertTrue(exception.getMessage().contains("Failed to get the constructor of resource class"));
    }

    @Test
    void shouldThrowExceptionWhenResourceClassHasStaticMethod() {
        List<Object> pathParams = List.of();    
     
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(StaticResourceMethod.class, HttpMethodName.GET, pathParams)
        );
     
        assertTrue(exception.getMessage().toLowerCase().contains("no public non-static method named"));
    }
}
