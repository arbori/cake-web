package cake.web.exchange.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for style-case detection, normalization, and conversion.
 * 
 * Supports:
 * - camelCase (e.g., "xRequestId", "customerOrder")
 * - PascalCase (e.g., "CustomerOrder", "XRequestId")
 * - kebab-case (e.g., "x-request-id", "customer-order")
 * - Train-Case (e.g., "X-Request-Id", "Content-Type")
 * - snake_case (e.g., "x_request_id", "customer_order")
 */
public final class StyleCase {

    private StyleCase() {
        // Prevent instantiation
    }

    /**
     * Converts any supported case format to lowerCamelCase.
     * Examples:
     * - "x-request-id" -> "xRequestId"
     * - "X-Request-Id" -> "xRequestId"
     * - "customer_order" -> "customerOrder"
     * - "CustomerOrder" -> "customerOrder"
     */
    public static String toCamelCase(String text) {
        if (text == null || text.isBlank())
            return text;
        List<String> words = splitWords(text);
        if (words.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder(words.get(0).toLowerCase(Locale.ROOT));
        for (int i = 1; i < words.size(); i++) {
            String word = words.get(i);
            sb.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }

    /**
     * Converts any supported case format to PascalCase (UpperCamelCase).
     * Examples:
     * - "customer-order" -> "CustomerOrder"
     * - "customer_order" -> "CustomerOrder"
     */
    public static String toPascalCase(String text) {
        if (text == null || text.isBlank())
            return text;
        List<String> words = splitWords(text);
        if (words.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }

    /**
     * Converts camelCase or PascalCase to kebab-case.
     * Example: "xRequestId" -> "x-request-id"
     */
    public static String toKebabCase(String text) {
        if (text == null || text.isBlank())
            return text;
        List<String> words = splitWords(text);
        return String.join("-", words.stream().map(w -> w.toLowerCase(Locale.ROOT)).toList());
    }

    /**
     * Converts camelCase to HTTP Header Train-Case.
     * Example: "xRequestId" -> "X-Request-Id", "traceId" -> "Trace-Id"
     */
    public static String toTrainCase(String text) {
        if (text == null || text.isBlank())
            return text;
        List<String> words = splitWords(text);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            if (i > 0)
                sb.append("-");
            sb.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }

    /**
     * Converts camelCase or PascalCase to snake_case.
     * Example: "minAge" -> "min_age", "customerOrder" -> "customer_order"
     */
    public static String toSnakeCase(String text) {
        if (text == null || text.isBlank())
            return text;
        List<String> words = splitWords(text);
        return String.join("_", words.stream().map(w -> w.toLowerCase(Locale.ROOT)).toList());
    }

    /**
     * Generates the standard JavaBeans setter name for a field.
     * Follows JavaBeans Spec Sec 8.8 (e.g., "xRequestId" -> "setxRequestId" or
     * "setXRequestId").
     */
    public static String toSetterName(String fieldName) {
        if (fieldName == null || fieldName.isEmpty())
            return "";
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    /**
     * Helper to split any string (kebab, snake, camel, Pascal) into individual
     * lowercase words.
     */
    public static List<String> splitWords(String text) {
        List<String> words = new ArrayList<>();
        if (text == null || text.isEmpty())
            return words;

        // Replace delimiters with space
        String cleaned = text.replaceAll("[-_.]+", " ");

        // Split camelCase boundaries (e.g., "xRequestId" -> "x Request Id")
        cleaned = cleaned.replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ");
        cleaned = cleaned.replaceAll("(?<=[A-Z])(?=[A-Z][a-z])", " ");

        for (String token : cleaned.trim().split("\\s+")) {
            if (!token.isEmpty()) {
                words.add(token);
            }
        }
        return words;
    }
}
