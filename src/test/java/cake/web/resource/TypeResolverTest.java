package cake.web.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TypeResolverTest {

    // ==================== TEST RESOURCE CLASSES ====================

    public static class SimpleResource {
        public void get() { /* only test */ }
        public void get(Integer id) { /* only test */ }
        public void get(String name) { /* only test */ }
        public void get(Long id) { /* only test */ }
        public void post(String name, Integer age) { /* only test */ }
        public void put(Integer id, String value) { /* only test */ }
        public void delete(UUID uuid) { /* only test */ }
    }

    public static class OverloadedResource {
        public void search(Integer id) { /* only test */ }
        public void search(String name) { /* only test */ }
        public void search(Long id, Integer page) { /* only test */ }
        public void search(String query, Integer page) { /* only test */ }
    }

    public static class MixedTypesResource {
        public void find(Integer id, String name) { /* only test */ }
        public void find(String name, Integer id) { /* only test */ }
        public void find(Integer id, Integer age) { /* only test */ }
        public void find(String firstName, String lastName) { /* only test */ }
    }

    public static class NoDefaultConstructorResource {
        public NoDefaultConstructorResource(String arg) { }
        public void get() { /* only test */ }
    }

    public static class EmptyResource {
        // No methods
    }

    // ==================== SUCCESS CASES ====================

    @Test
    void shouldResolveMethodWithZeroParameters() throws Exception {
        // Arrange
        List<String> pathParams = List.of();
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, "get", pathParams
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
        List<String> pathParams = List.of("123");
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, "get", pathParams
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
        List<String> pathParams = List.of("john");
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, "get", pathParams
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
        List<String> pathParams = List.of("john", "25");
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, "post", pathParams
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
        List<String> pathParams = List.of(uuidString);
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, "delete", pathParams
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
        List<String> pathParams = List.of("123");
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(OverloadedResource.class, "search", pathParams)
        );
        
        assertTrue(exception.getMessage().contains("ambiguous"));
    }

    @Test
    void shouldThrowExceptionWhenMultipleMethodsMatchWithMultipleParameters() {
        // Arrange
        List<String> pathParams = List.of("123", "1");
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(OverloadedResource.class, "search", pathParams)
        );
        
        assertTrue(exception.getMessage().contains("ambiguous"));
    }

    // ==================== NOT FOUND CASES ====================

    @Test
    void shouldThrowExceptionWhenNoMethodWithMatchingNameExists() {
        // Arrange
        List<String> pathParams = List.of();
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(SimpleResource.class, "nonexistent", pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No compatible method"));
        assertTrue(exception.getMessage().contains("nonexistent"));
    }

    @Test
    void shouldThrowExceptionWhenNoMethodWithCompatibleParametersExists() {
        // Arrange
        List<String> pathParams = List.of("123", "456", "789"); // 3 params, but no method with 3 params
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(SimpleResource.class, "get", pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No compatible method"));
    }

    @Test
    void shouldThrowExceptionWhenResourceClassHasNoMethods() {
        // Arrange
        List<String> pathParams = List.of();
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(EmptyResource.class, "get", pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No compatible method"));
    }

    @Test
    void shouldThrowExceptionWhenParameterTypesDoNotMatch() {
        // Arrange
        List<String> pathParams = List.of("not a number");
        
        // Act & Assert - SimpleResource has get(Integer) but value is not a number
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(SimpleResource.class, "get", pathParams)
        );
        
        assertTrue(exception.getMessage().contains("No compatible method"));
    }

    // ==================== NULL AND EDGE CASES ====================

    @Test
    void shouldThrowExceptionWhenResourceClassIsNull() {
        List<String> pathParams = List.of();
        
        assertThrows(IllegalArgumentException.class, () ->
            TypeResolver.methodResolution(null, "get", pathParams)
        );
    }

    @Test
    void shouldThrowExceptionWhenMethodNameIsNull() {
        List<String> pathParams = List.of();
        
        assertThrows(IllegalArgumentException.class, () ->
            TypeResolver.methodResolution(SimpleResource.class, null, pathParams)
        );
    }

    @Test
    void shouldThrowExceptionWhenPathParamsIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
            TypeResolver.methodResolution(SimpleResource.class, "get", null)
        );
    }

    @Test
    void shouldHandleEmptyPathParams() throws Exception {
        // Arrange
        List<String> pathParams = List.of();
        
        // Act
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, "get", pathParams
        );
        
        // Assert
        assertNotNull(resolution);
        assertEquals(0, resolution.method().getParameterCount());
    }

    // ==================== TYPE SPECIFICITY CASES ====================

    @Test
    void shouldPreferIntegerOverLongWhenBothCompatible() throws Exception {
        // SimpleResource has: get(Integer) and get(Long)
        // "123" fits in both Integer and Long
        // Should match the first one found? Actually Convertion.convert returns Integer for "123"
        // So get(Integer) should match
        
        List<String> pathParams = List.of("123");
        
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, "get", pathParams
        );
        
        assertNotNull(resolution);
        assertEquals(Integer.class, resolution.method().getParameterTypes()[0]);
    }

    @Test
    void shouldMatchStringWhenNumericConversionFails() throws Exception {
        // SimpleResource has: get(String) and get(Integer)
        // "abc" cannot be Integer, so should match get(String)
        
        List<String> pathParams = List.of("abc");
        
        MethodResolution resolution = TypeResolver.methodResolution(
            SimpleResource.class, "get", pathParams
        );
        
        assertNotNull(resolution);
        assertEquals(String.class, resolution.method().getParameterTypes()[0]);
        assertEquals("abc", resolution.args().get(0));
    }

    @Test
    void shouldResolveWhenMethodHasMixedParameterTypes() throws Exception {
        // MixedTypesResource has multiple find methods
        // find(Integer id, String name) and find(String name, Integer id)
        // With pathParams ["123", "john"], both are compatible
        // Should be ambiguous
        
        List<String> pathParams = List.of("123", "john");
        
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            TypeResolver.methodResolution(MixedTypesResource.class, "find", pathParams)
        );
        
        assertTrue(exception.getMessage().contains("ambiguous"));
    }
}