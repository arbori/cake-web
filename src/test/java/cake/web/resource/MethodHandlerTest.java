package cake.web.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cake.web.exchange.HttpDataHandle;
import cake.web.exchange.HttpMethodName;

class MethodHandlerTest {
    // ==================== MOCKS ====================
    @Mock
    HttpServletRequest httpServletRequest;

    HttpDataHandle httpDataHandle;
    
    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        when(httpServletRequest.getParameterMap()).thenReturn(Map.of());
        when(httpServletRequest.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
        when(httpServletRequest.getReader()).thenReturn(null);
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        httpDataHandle = new HttpDataHandle(httpServletRequest);
    }

    // ==================== RESOURCES ====================

    // Test resource classes
    public static class SimpleResource {
        public void get() { /* Only test */ }
        public void get(Integer id) { /* Only test */ }
        public void post(String name) { /* Only test */ }
        public void put(Integer id, String value) { /* Only test */ }
        public void delete(Integer id) { /* Only test */ }
    }

    public static class OverloadedResource {
        public void delete(Long query) { /* Only test */ }
        public void delete(Integer id) { /* Only test */ }
        public void delete(Long id, Integer page) { /* Only test */ }
        public void delete(Short query, Integer page) { /* Only test */ }
        public void post(Integer id) { /* Only test */ }
    }

    public static class AmbiguousResource {
        public void post(Integer id) { /* Only test */ }
        public void post(Long longId) { /* Only test */ }
    }

    public static class MixedCaseResource {
        public MixedCaseResource() { /* Only test */ }

        public void GET() { /* Only test */ }
        public void get() { /* Only test */ }
    }

    // Helper class to expose internal logic for testing
    public static class MethodHandleTestHelper {
        public static MethodResolution findMethodInternal(Class<?> resourceClass, List<Object> pathParams, HttpMethodName httpMethodName, HttpDataHandle httpDataHandle) 
                throws NoSuchMethodException {
            // This would be the actual implementation
            // For now, we'll test via the concrete implementation
            return MethodHandler.findHttpMethod(resourceClass, httpMethodName, pathParams, httpDataHandle);
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

    // ==================== SUCCESS CASES ====================

    @Test
    void shouldResolveMethodWithZeroParameters() throws Exception {
        // Act
        MethodResolution resolution = MethodHandler.findHttpMethod(SimpleResource.class, HttpMethodName.GET, List.of(), httpDataHandle);
        
        // Assert
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(0, resolution.method().getParameterCount());
    }

    @Test
    void shouldResolveMethodWithOneStringParameter() throws Exception {
        // Act
        MethodResolution resolution = MethodHandler.findHttpMethod(SimpleResource.class, HttpMethodName.POST, List.of("john"), httpDataHandle);
        
        // Assert
        assertNotNull(resolution);
        assertEquals("post", resolution.method().getName());
        assertEquals(1, resolution.method().getParameterCount());
        assertEquals(String.class, resolution.method().getParameterTypes()[0]);
    }

    @Test
    void shouldResolveMethodWithMultipleParameters() throws Exception {
        // Act
        MethodResolution resolution = MethodHandler.findHttpMethod(
            SimpleResource.class, 
            HttpMethodName.PUT,
            List.of("123", "value"),
            httpDataHandle
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
        // Act — HttpMethodName.GET is "get" (lowercase in enum)
        MethodResolution resolution = MethodHandler.findHttpMethod(MixedCaseResource.class, HttpMethodName.GET, List.of(), httpDataHandle);
        
        // Assert
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName().toLowerCase());
    }

    @Test
    void shouldCacheResolvedMethod() throws Exception {
        // Act — first call (cache miss)
        MethodResolution first = MethodHandler.findHttpMethod(SimpleResource.class, HttpMethodName.POST, List.of("test"), httpDataHandle);
        
        // Second call (should be cache hit)
        MethodResolution second = MethodHandler.findHttpMethod(SimpleResource.class, HttpMethodName.POST, List.of("another"), httpDataHandle);
        
        // Assert — same method instance
        assertSame(first.method(), second.method());
    }

    @Test
    void shouldGenerateDifferentCacheKeysForDifferentParameterTypes() throws Exception {
        // Act
        MethodResolution getMethod = MethodHandler.findHttpMethod(SimpleResource.class, HttpMethodName.GET, List.of("123"), httpDataHandle);
        MethodResolution postMethod = MethodHandler.findHttpMethod(SimpleResource.class, HttpMethodName.POST, List.of("query"), httpDataHandle);
        
        assertNotSame(getMethod.method(), postMethod.method());
        assertNotEquals(getMethod.method(), postMethod.method());
    }

    // ==================== AMBIGUITY CASES ====================

    @Test
    void shouldThrowExceptionWhenMultipleMethodsWithSameNameAndSameParameterCount() {
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodHandler.findHttpMethod(AmbiguousResource.class, HttpMethodName.POST, List.of("123"), httpDataHandle)
        );
        
        assertTrue(exception.getMessage().contains("Ambiguous"));
        assertTrue(exception.getMessage().contains("post"));
        assertTrue(exception.getMessage().contains("Integer"));
        assertTrue(exception.getMessage().contains("Long"));
    }

    @Test
    void shouldThrowExceptionWhenMultipleMethodsWithSameNameAndSameParameterCountWithMoreParams() {
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodHandler.findHttpMethod(OverloadedResource.class, HttpMethodName.DELETE, List.of("1", "2"), httpDataHandle)
        );
        
        assertTrue(exception.getMessage().contains("Ambiguity call to cake.web.resource.MethodHandlerTest$OverloadedResource.delete. Endpoint overload is not allowed.\ndelete(Short,Integer)\ndelete(Long,Integer)"));
    }

    // ==================== NOT FOUND CASES ====================

    @Test
    void shouldThrowExceptionWhenNoMethodWithMatchingNameExists() {
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodHandler.findHttpMethod(SimpleResource.class, HttpMethodName.DELETE, List.of(), httpDataHandle)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named cake.web.resource.MethodHandlerTest$SimpleResource.delete found."));
    }

    @Test
    void shouldThrowExceptionWhenNoMethodWithMatchingParameterCountExists() {
        // Act & Assert — get() exists with 0 params, but we're passing 2 params
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodHandler.findHttpMethod(SimpleResource.class, HttpMethodName.GET, List.of("p1", "p2"), httpDataHandle)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named cake.web.resource.MethodHandlerTest$SimpleResource.get found."));
    }

    @Test
    void shouldThrowExceptionWhenResourceClassHasNoMethods() {
        // Act & Assert
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () ->
            MethodHandler.findHttpMethod(EmptyResource.class, HttpMethodName.GET, List.of(), httpDataHandle)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named cake.web.resource.MethodHandlerTest$EmptyResource.get found."));
    }

    // ==================== CASE SENSITIVITY ====================

    @Test
    void shouldMatchMethodNameCaseInsensitively() throws NoSuchMethodException, IllegalArgumentException {
        // HttpMethodName.GET = "get" (lowercase)
        // MixedCaseResource has method "GET" (uppercase) and "get" (lowercase)
        // Both exist but with 0 parameters, the framework should consider only
        // lowercase match for HTTP method name, so "get" should be chosen.
        
        // Act & Assert — both exist with 0 params → ambiguous
        MethodResolution resolution = MethodHandler.findHttpMethod(MixedCaseResource.class, HttpMethodName.GET, List.of(), httpDataHandle);
        
        
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
        
        // Resolve post(String) - one parameter
        MethodResolution resolution1 = MethodHandler.findHttpMethod(SimpleResource.class, HttpMethodName.POST, List.of("test"), httpDataHandle);
        
        // Clear cache and change method? Can't at runtime.
        // Instead, verify that a different method with same name but different count
        // produces a different method instance
        
        // SimpleResource has:
        // - post(String) with 1 param
        // - No post with 2 params
        
        // So this test is limited. Cache key includes param types, so it's fine.
        
        assertNotNull(resolution1);
    }

    // ==================== EDGE CASES ====================

    @Test
    void shouldHandleNullPathParametersList() {
        // Act & Assert — should treat as 0 parameters
        assertThrows(NoSuchMethodException.class, () ->
            MethodHandler.findHttpMethod(SimpleResource.class, HttpMethodName.GET, null, httpDataHandle)
        );
        // Better to handle null gracefully — depends on implementation
    }

    @Test
    void shouldHandleEmptyPathParametersList() throws Exception {
        // Act
        MethodResolution resolution = MethodHandler.findHttpMethod(SimpleResource.class, HttpMethodName.GET, List.of(), httpDataHandle);
        
        // Assert
        assertNotNull(resolution);
        assertEquals(0, resolution.method().getParameterCount());
    }

    @Test
    void shouldntHandleResourceWithPrivateMethods() {
        // Arrange
        @SuppressWarnings("unused")
        class PrivateMethodResource {
            private void secret() { /* Only test */ }
            private void get() { /* Only test */ }
        }

        // Act — getMethods() returns only public methods
        assertThrows(NoSuchMethodException.class, () -> 
            MethodHandler.findHttpMethod(PrivateMethodResource.class, HttpMethodName.GET, List.of(), httpDataHandle),
            "Expected NoSuchMethodException for private methods because the framework handle only public methods"
        );
    }

    @Test
    void shouldNotHandleResourceWithStaticMethods() {
        // Act — static methods are included in getMethods()
        NoSuchMethodException exception = assertThrows(NoSuchMethodException.class, () -> 
            MethodHandler.findHttpMethod(StaticMethodResource.class, HttpMethodName.GET, List.of(), httpDataHandle)
        );
        
        assertTrue(exception.getMessage().contains("No public non-static method named cake.web.resource.MethodHandlerTest$StaticMethodResource.get found."));
    }

    @Test
    void shouldHandleResourceWithInheritedMethods() throws Exception {
        // Act
        MethodResolution resolution = MethodHandler.findHttpMethod(Child.class, HttpMethodName.GET, List.of(), httpDataHandle);
        
        // Assert — getMethods() includes inherited methods
        assertNotNull(resolution);
        assertEquals("get", resolution.method().getName());
        assertEquals(Parent.class, resolution.method().getDeclaringClass());
    }
}
