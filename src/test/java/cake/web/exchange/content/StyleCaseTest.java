package cake.web.exchange.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StyleCaseTest {

    // ==================== splitWords TESTS ====================
    @Nested
    class SplitWordsTests {

        @Test
        void shouldHandleNullAndEmpty() {
            assertTrue(StyleCase.splitWords(null).isEmpty());
            assertTrue(StyleCase.splitWords("").isEmpty());
            assertTrue(StyleCase.splitWords("   ").isEmpty());
        }

        @Test
        void shouldSplitSingleWord() {
            List<String> words = StyleCase.splitWords("customer");
            assertEquals(List.of("customer"), words);
        }

        @Test
        void shouldSplitKebabCase() {
            List<String> words = StyleCase.splitWords("x-request-id");
            assertEquals(List.of("x", "request", "id"), words);
        }

        @Test
        void shouldSplitTrainCase() {
            List<String> words = StyleCase.splitWords("X-Request-Id");
            assertEquals(List.of("X", "Request", "Id"), words);
        }

        @Test
        void shouldSplitSnakeCase() {
            List<String> words = StyleCase.splitWords("customer_order_item");
            assertEquals(List.of("customer", "order", "item"), words);
        }

        @Test
        void shouldSplitScreamingSnakeCase() {
            List<String> words = StyleCase.splitWords("CUSTOMER_ORDER");
            assertEquals(List.of("CUSTOMER", "ORDER"), words);
        }

        @Test
        void shouldSplitCamelCase() {
            List<String> words = StyleCase.splitWords("xRequestId");
            assertEquals(List.of("x", "Request", "Id"), words);
        }

        @Test
        void shouldSplitPascalCase() {
            List<String> words = StyleCase.splitWords("CustomerOrder");
            assertEquals(List.of("Customer", "Order"), words);
        }

        @Test
        void shouldSplitAcronymsInCamelCase() {
            List<String> words = StyleCase.splitWords("XMLParserRequest");
            assertEquals(List.of("XML", "Parser", "Request"), words);
        }
    }

    // ==================== toCamelCase TESTS ====================
    @Nested
    class ToCamelCaseTests {

        @Test
        void shouldHandleNullAndBlank() {
            assertNull(StyleCase.toCamelCase(null));
            assertEquals("", StyleCase.toCamelCase(""));
            assertEquals("   ", StyleCase.toCamelCase("   "));
        }

        @Test
        void shouldConvertKebabCaseToCamelCase() {
            assertEquals("xRequestId", StyleCase.toCamelCase("x-request-id"));
            assertEquals("traceId", StyleCase.toCamelCase("trace-id"));
            assertEquals("contentType", StyleCase.toCamelCase("content-type"));
        }

        @Test
        void shouldConvertTrainCaseToCamelCase() {
            assertEquals("xRequestId", StyleCase.toCamelCase("X-Request-Id"));
            assertEquals("traceId", StyleCase.toCamelCase("Trace-Id"));
            assertEquals("contentType", StyleCase.toCamelCase("Content-Type"));
        }

        @Test
        void shouldConvertSnakeCaseToCamelCase() {
            assertEquals("customerOrder", StyleCase.toCamelCase("customer_order"));
            assertEquals("minAge", StyleCase.toCamelCase("min_age"));
            assertEquals("fiscalNumber", StyleCase.toCamelCase("fiscal_number"));
        }

        @Test
        void shouldConvertPascalCaseToCamelCase() {
            assertEquals("customerRequest", StyleCase.toCamelCase("CustomerRequest"));
            assertEquals("addressResponse", StyleCase.toCamelCase("AddressResponse"));
        }

        @Test
        void shouldPreserveCamelCase() {
            assertEquals("xRequestId", StyleCase.toCamelCase("xRequestId"));
            assertEquals("minAge", StyleCase.toCamelCase("minAge"));
        }

        @Test
        void shouldConvertAcronymsToCamelCase() {
            assertEquals("xmlParserRequest", StyleCase.toCamelCase("XMLParserRequest"));
            assertEquals("dtoAddress", StyleCase.toCamelCase("DTOAddress"));
        }
    }

    // ==================== toPascalCase TESTS ====================
    @Nested
    class ToPascalCaseTests {

        @Test
        void shouldHandleNullAndBlank() {
            assertNull(StyleCase.toPascalCase(null));
            assertEquals("", StyleCase.toPascalCase(""));
            assertEquals("   ", StyleCase.toPascalCase("   "));
        }

        @Test
        void shouldConvertKebabCaseToPascalCase() {
            assertEquals("CustomerOrder", StyleCase.toPascalCase("customer-order"));
            assertEquals("AddressCapture", StyleCase.toPascalCase("address-capture"));
        }

        @Test
        void shouldConvertSnakeCaseToPascalCase() {
            assertEquals("CustomerOrder", StyleCase.toPascalCase("customer_order"));
        }

        @Test
        void shouldConvertCamelCaseToPascalCase() {
            assertEquals("CustomerOrder", StyleCase.toPascalCase("customerOrder"));
            assertEquals("XRequestId", StyleCase.toPascalCase("xRequestId"));
        }

        @Test
        void shouldConvertSingleWordToPascalCase() {
            assertEquals("Customer", StyleCase.toPascalCase("customer"));
            assertEquals("Address", StyleCase.toPascalCase("address"));
        }
    }

    // ==================== toKebabCase TESTS ====================
    @Nested
    class ToKebabCaseTests {

        @Test
        void shouldHandleNullAndBlank() {
            assertNull(StyleCase.toKebabCase(null));
            assertEquals("", StyleCase.toKebabCase(""));
            assertEquals("   ", StyleCase.toKebabCase("   "));
        }

        @Test
        void shouldConvertCamelCaseToKebabCase() {
            assertEquals("x-request-id", StyleCase.toKebabCase("xRequestId"));
            assertEquals("trace-id", StyleCase.toKebabCase("traceId"));
            assertEquals("min-age", StyleCase.toKebabCase("minAge"));
        }

        @Test
        void shouldConvertPascalCaseToKebabCase() {
            assertEquals("customer-order", StyleCase.toKebabCase("CustomerOrder"));
            assertEquals("address-request", StyleCase.toKebabCase("AddressRequest"));
        }

        @Test
        void shouldConvertSnakeCaseToKebabCase() {
            assertEquals("customer-order", StyleCase.toKebabCase("customer_order"));
        }

        @Test
        void shouldConvertAcronymsToKebabCase() {
            assertEquals("xml-parser", StyleCase.toKebabCase("XMLParser"));
        }
    }

    // ==================== toTrainCase TESTS ====================
    @Nested
    class ToTrainCaseTests {

        @Test
        void shouldHandleNullAndBlank() {
            assertNull(StyleCase.toTrainCase(null));
            assertEquals("", StyleCase.toTrainCase(""));
            assertEquals("   ", StyleCase.toTrainCase("   "));
        }

        @Test
        void shouldConvertCamelCaseToTrainCase() {
            assertEquals("X-Request-Id", StyleCase.toTrainCase("xRequestId"));
            assertEquals("Trace-Id", StyleCase.toTrainCase("traceId"));
            assertEquals("Content-Type", StyleCase.toTrainCase("contentType"));
        }

        @Test
        void shouldConvertKebabCaseToTrainCase() {
            assertEquals("X-Request-Id", StyleCase.toTrainCase("x-request-id"));
            assertEquals("Trace-Id", StyleCase.toTrainCase("trace-id"));
        }

        @Test
        void shouldConvertSingleWordToTrainCase() {
            assertEquals("Authorization", StyleCase.toTrainCase("authorization"));
        }
    }

    // ==================== toSnakeCase TESTS ====================
    @Nested
    class ToSnakeCaseTests {

        @Test
        void shouldHandleNullAndBlank() {
            assertNull(StyleCase.toSnakeCase(null));
            assertEquals("", StyleCase.toSnakeCase(""));
            assertEquals("   ", StyleCase.toSnakeCase("   "));
        }

        @Test
        void shouldConvertCamelCaseToSnakeCase() {
            assertEquals("min_age", StyleCase.toSnakeCase("minAge"));
            assertEquals("customer_order", StyleCase.toSnakeCase("customerOrder"));
            assertEquals("fiscal_number", StyleCase.toSnakeCase("fiscalNumber"));
        }

        @Test
        void shouldConvertPascalCaseToSnakeCase() {
            assertEquals("customer_request", StyleCase.toSnakeCase("CustomerRequest"));
            assertEquals("customer_order", StyleCase.toSnakeCase("CustomerOrder"));
        }

        @Test
        void shouldConvertKebabCaseToSnakeCase() {
            assertEquals("x_request_id", StyleCase.toSnakeCase("x-request-id"));
            assertEquals("min_age", StyleCase.toSnakeCase("min-age"));
        }
    }

    // ==================== toSetterName TESTS ====================
    @Nested
    class ToSetterNameTests {

        @Test
        void shouldHandleNullAndEmpty() {
            assertEquals("", StyleCase.toSetterName(null));
            assertEquals("", StyleCase.toSetterName(""));
        }

        @Test
        void shouldGenerateSetterForSimpleField() {
            assertEquals("setName", StyleCase.toSetterName("name"));
            assertEquals("setCity", StyleCase.toSetterName("city"));
            assertEquals("setAge", StyleCase.toSetterName("age"));
        }

        @Test
        void shouldGenerateSetterForCamelCaseField() {
            assertEquals("setFiscalNumber", StyleCase.toSetterName("fiscalNumber"));
            assertEquals("setXRequestId", StyleCase.toSetterName("xRequestId"));
        }
    }
}
