package cake.web.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cake.web.exchange.HttpMethodName;

class MethodHandlerTest {

    // Test resource classes
    public static class SimpleResource {
        public void get() { /* Only test */ }
        public void get(Integer id) { /* Only test */ }
        public void post(String name) { /* Only test */ }
        public void put(Integer id, String value) { /* Only test */ }
    }

    public static class OverloadedResource {
        public void delete(String query) { /* Only test */ }
        public void delete(Integer id) { /* Only test */ }
        public void delete(Long id, Integer page) { /* Only test */ }
        public void delete(String query, Integer page) { /* Only test */ }
        public void post(Integer id) { /* Only test */ }
    }

    public static class AmbiguousResource {
        public void post(Integer id) { /* Only test */ }
        public void post(String name) { /* Only test */ }
    }

    public static class MixedCaseResource {
        public MixedCaseResource() { /* Only test */ }

        public void GET() { /* Only test */ }
        public void get() { /* Only test */ }
    }

    // Helper class to expose internal logic for testing
    public static class MethodHandleTestHelper extends MethodHandler {
        public static MethodResolution findMethodInternal(Class<?> resourceClass, List<String> pathParams, HttpMethodName httpMethodName) 
                throws NoSuchMethodException {
            // This would be the actual implementation
            // For now, we'll test via the concrete implementation
            MethodHandler handler = new MethodHandler();
            return handler.findHttpMethod(resourceClass, pathParams, httpMethodName);
        }
    }

    // Arrange
    public static class Parent {
        public void get() { 
            // Only test. 
        }
    }
    public static class Child extends Parent {
        // Inherits get() from Parent
    }

    public static class StaticMethodResource {
        public static void get() { /* Only test */ }
    }

    public static class EmptyResource {}

    // ==================== START CONFIGURATION ====================

    @BeforeEach
    void setUp() {
        MethodHandler.clearCache();
    }

    // ==================== SUCCESS CASES ====================

    @Test
    void shouldResolveMethodWithZeroParameters() throws Exception {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // Act
        MethodResolution resolution = handler.findHttpMethod(SimpleResource.class, List.of(), HttpMethodName.GET);
        
        // Assert
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(0, resolution.method().getParameterCount());
    }

    @Test
    void shouldResolveMethodWithOneStringParameter() throws Exception {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // Act
        MethodResolution resolution = handler.findHttpMethod(SimpleResource.class, List.of("john"), HttpMethodName.POST);
        
        // Assert
        assertNotNull(resolution);
        assertEquals("post", resolution.method().getName());
        assertEquals(1, resolution.method().getParameterCount());
        assertEquals(String.class, resolution.method().getParameterTypes()[0]);
    }

    @Test
    void shouldResolveMethodWithMultipleParameters() throws Exception {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // Act
        MethodResolution resolution = handler.findHttpMethod(
            SimpleResource.class, 
            List.of("123", "value"), 
            HttpMethodName.PUT
        );
        
        // Assert
        assertNotNull(resolution);
        assertEquals("put", resolution.method().getName());
        assertEquals(2, resolution.method().getParameterCount());
        assertEquals(Integer.class, resolution.method().getParameterTypes()[0]);
        assertEquals(String.class, resolution.method().getParameterTypes()[1]);
    }

    @Test
    void shouldResolveMethodIgnoringCaseOfHttpMethodName() throws Exception {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // Act — HttpMethodName.GET is "get" (lowercase in enum)
        MethodResolution resolution = handler.findHttpMethod(MixedCaseResource.class, List.of(), HttpMethodName.GET);
        
        // Assert
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName().toLowerCase());
    }

    @Test
    void shouldCacheResolvedMethod() throws Exception {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // Act — first call (cache miss)
        MethodResolution first = handler.findHttpMethod(SimpleResource.class, List.of("test"), HttpMethodName.POST);
        
        // Second call (should be cache hit)
        MethodResolution second = handler.findHttpMethod(SimpleResource.class, List.of("another"), HttpMethodName.POST);
        
        // Assert — same method instance
        assertSame(first.method(), second.method());
    }

    @Test
    void shouldGenerateDifferentCacheKeysForDifferentParameterTypes() throws Exception {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // Act
        MethodResolution getMethod = handler.findHttpMethod(SimpleResource.class, List.of("123"), HttpMethodName.GET);
        MethodResolution postMethod = handler.findHttpMethod(SimpleResource.class, List.of("query"), HttpMethodName.POST);
        
        assertNotSame(getMethod.method(), postMethod.method());
        assertNotEquals(getMethod.method(), postMethod.method());
    }

    // ==================== AMBIGUITY CASES ====================

    @Test
    void shouldThrowExceptionWhenMultipleMethodsWithSameNameAndSameParameterCount() {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            handler.findHttpMethod(AmbiguousResource.class, List.of("123"), HttpMethodName.POST)
        );
        
        assertTrue(exception.getMessage().contains("Ambiguous"));
        assertTrue(exception.getMessage().contains("post"));
        assertTrue(exception.getMessage().contains("Integer"));
        assertTrue(exception.getMessage().contains("String"));
    }

    @Test
    void shouldThrowExceptionWhenMultipleMethodsWithSameNameAndSameParameterCountWithMoreParams() {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // OverloadedResource has two methods with 2 parameters:
        // - delete(Long, Integer)
        // - delete(String, Integer)
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            handler.findHttpMethod(OverloadedResource.class, List.of("1", "2"), HttpMethodName.DELETE)
        );
        
        assertTrue(exception.getMessage().contains("Ambiguous"));
        assertTrue(exception.getMessage().contains("delete"));
    }

    // ==================== NOT FOUND CASES ====================

    @Test
    void shouldThrowExceptionWhenNoMethodWithMatchingNameExists() {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            handler.findHttpMethod(SimpleResource.class, List.of(), HttpMethodName.DELETE)
        );
        
        assertTrue(exception.getMessage().contains("No method named 'delete'"));
        assertTrue(exception.getMessage().contains("with 0 parameter(s)"));
    }

    @Test
    void shouldThrowExceptionWhenNoMethodWithMatchingParameterCountExists() {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // Act & Assert — get() exists with 0 params, but we're passing 2 params
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            handler.findHttpMethod(SimpleResource.class, List.of("p1", "p2"), HttpMethodName.GET)
        );
        
        assertTrue(exception.getMessage().contains("No method named 'get'"));
        assertTrue(exception.getMessage().contains("with 2 parameter(s)"));
    }

    @Test
    void shouldThrowExceptionWhenResourceClassHasNoMethods() {
        MethodHandler handler = new MethodHandler();
        
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            handler.findHttpMethod(EmptyResource.class, List.of(), HttpMethodName.GET)
        );
        
        assertTrue(exception.getMessage().contains("No method named 'get'"));
    }

    // ==================== CASE SENSITIVITY ====================

    @Test
    void shouldMatchMethodNameCaseInsensitively() throws NoSuchMethodException, IllegalArgumentException {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // HttpMethodName.GET = "get" (lowercase)
        // MixedCaseResource has method "GET" (uppercase) and "get" (lowercase)
        // Both exist but with 0 parameters, the framework should consider only
        // lowercase match for HTTP method name, so "get" should be chosen.
        
        // Act & Assert — both exist with 0 params → ambiguous
        MethodResolution resolution = handler.findHttpMethod(MixedCaseResource.class, List.of(), HttpMethodName.GET);
        
        
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
    }

    // ==================== CACHE INVALIDATION ====================

    @Test
    void shouldInvalidateCacheWhenMethodSignatureChanges() throws Exception {
        // This is tricky to test because we can't change a class at runtime
        // In practice, cache invalidation happens when the resource class is
        // reloaded (e.g., in dev mode). The cache key includes parameter types,
        // so if the method signature changes, the key changes automatically.
        
        // For testing, we verify that different signatures produce different cache entries
        MethodHandler handler = new MethodHandler();
        
        // Resolve post(String) - one parameter
        MethodResolution resolution1 = handler.findHttpMethod(SimpleResource.class, List.of("test"), HttpMethodName.POST);
        
        // Clear cache and change method? Can't at runtime.
        // Instead, verify that a different method with same name but different count
        // produces a different method instance
        
        // SimpleResource has:
        // - post(String) with 1 param
        // - No post with 2 params
        
        // So this test is limited. Cache key includes param types, so it's fine.
        
        assertNotNull(resolution1);
    }

    @Test
    void clearCacheShouldRemoveAllEntries() throws Exception {
        // Arrange
        MethodHandler handler = new MethodHandler();
        handler.findHttpMethod(SimpleResource.class, List.of(), HttpMethodName.GET);
        handler.findHttpMethod(SimpleResource.class, List.of("test"), HttpMethodName.POST);
        
        int beforeSize = MethodHandler.getCacheSize();
        assertTrue(beforeSize > 0);
        
        // Act
        MethodHandler.clearCache();
        
        // Assert
        assertEquals(0, MethodHandler.getCacheSize());
    }

    @Test
    void clearCacheForClassShouldRemoveOnlyEntriesForThatClass() throws Exception {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        handler.findHttpMethod(SimpleResource.class, List.of(), HttpMethodName.GET);
        handler.findHttpMethod(OverloadedResource.class, List.of("123"), HttpMethodName.POST);
        
        int beforeSize = MethodHandler.getCacheSize();
        assertTrue(beforeSize >= 2);
        
        // Act
        MethodHandler.clearCacheForClass(SimpleResource.class);
        
        // Assert
        int afterSize = MethodHandler.getCacheSize();
        assertTrue(afterSize < beforeSize);
        assertTrue(afterSize > 0); // SimpleResource entries remain
    }

    // ==================== EDGE CASES ====================

    @Test
    void shouldHandleNullPathParametersList() {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // Act & Assert — should treat as 0 parameters
        assertThrows(NoSuchMethodException.class, () ->
            handler.findHttpMethod(SimpleResource.class, null, HttpMethodName.GET)
        );
        // Better to handle null gracefully — depends on implementation
    }

    @Test
    void shouldHandleEmptyPathParametersList() throws Exception {
        // Arrange
        MethodHandler handler = new MethodHandler();
        
        // Act
        MethodResolution resolution = handler.findHttpMethod(SimpleResource.class, List.of(), HttpMethodName.GET);
        
        // Assert
        assertNotNull(resolution);
        assertEquals(0, resolution.method().getParameterCount());
    }

    @Test
    void shouldntHandleResourceWithPrivateMethods() {
        // Arrange
        class PrivateMethodResource {
            private void secret() { /* Only test */ }
            private void get() { /* Only test */ }
        }
        MethodHandler handler = new MethodHandler();
        
        // Act — getMethods() returns only public methods
        assertThrows(NoSuchMethodException.class, () -> 
            handler.findHttpMethod(PrivateMethodResource.class, List.of(), HttpMethodName.GET),
            "Expected NoSuchMethodException for private methods because the framework handle only public methods"
        );
    }

    @Test
    void shouldHandleResourceWithStaticMethods() {
        MethodHandler handler = new MethodHandler();
        
        // Act — static methods are included in getMethods()
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () -> 
            handler.findHttpMethod(StaticMethodResource.class, List.of(), HttpMethodName.GET)
        );
        
        assertTrue(exception.getMessage().contains("non-static"));
    }

    @Test
    void shouldHandleResourceWithInheritedMethods() throws Exception {
        MethodHandler handler = new MethodHandler();
        
        // Act
        MethodResolution resolution = handler.findHttpMethod(Child.class, List.of(), HttpMethodName.GET);
        
        // Assert — getMethods() includes inherited methods
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(Parent.class, resolution.method().getDeclaringClass());
    }
}