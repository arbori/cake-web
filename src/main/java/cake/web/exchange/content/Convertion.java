package cake.web.exchange.content;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Convertion {
    private Convertion() {
        // static class
    }

    private static final String INTEGER_REGEX = "^-?\\d+$";
    private static final String FLOATING_POINT_REGEX = "^-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?$";
    private static final String LOCAL_TIME_REGEX = "^\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?$";
    private static final String LOCAL_DATE_REGEX = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String OFFSET_DATE_TIME_REGEX = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)$";
    private static final String OFFSET_TIME_REGEX = "^\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)$";
    private static final String ZONED_DATE_TIME_REGEX = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)(\\[.*\\])?$";
    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Converts the given object to the specified target type. The conversion logic is based on the format of the input value and the target type.
     * Supported conversions include:
     * - Numeric types (byte, short, int, long, float, double, BigInteger, BigDecimal)
     * - Date/time types (LocalTime, LocalDate, LocalDateTime, OffsetDateTime, OffsetTime, ZonedDateTime)
     * - Boolean type
     * - UUID type
     * - JSON deserialization for types implementing BodyContent
     * - String and Object types (returns the string value)
     *
     * @param object the input object to convert (typically a string representation of a path parameter)
     * @param targetType the class of the target type to convert to
     * @return the converted object of the target type
     * @throws IllegalArgumentException if the conversion cannot be performed due to unsupported types or invalid formats
     */
    public static Object convert(Object object, Class<?> targetType) {
        if (object == null) {
            return null;
        }

        if(targetType.isInstance(object)) {
            return object;
        }

        if(targetType == BodyContent.class && object instanceof String bodyContent) {
            return toJson(bodyContent, targetType);
        }

        return convertString(object.toString(), targetType);
    }

    /**
     * Converts a string value to the specified target type.
     *
     * @param value the string value to convert
     * @param targetType the class of the target type to convert to
     * @return the converted object of the target type
     * @throws IllegalArgumentException if the conversion cannot be performed due to unsupported types or invalid formats
     */
    public static Object convertString(String value, Class<?> targetType) {
        // Check if the value looks like any integer value (byte, short, int, long)
        if (value.matches(INTEGER_REGEX) || value.matches(FLOATING_POINT_REGEX)) {
            return toNumber(value, targetType);
        }
        
        // Check if the value looks like a date/time string (e.g., "2023-08-15T14:30:00Z", "14:30:00", "2023-08-15T14:30:00", etc.)
        if(value.matches(LOCAL_TIME_REGEX) ||
            value.matches(LOCAL_DATE_REGEX) ||
            value.matches(OFFSET_DATE_TIME_REGEX) ||
            value.matches(OFFSET_TIME_REGEX) ||
            value.matches(ZONED_DATE_TIME_REGEX))
        {
            return toDateTime(value, targetType);
        }

        if (("true".equals(value.toLowerCase().trim()) || "false".equals(value.toLowerCase().trim())) && targetType == Boolean.class) {
            return Boolean.valueOf(value);
        }

        if(value.matches(UUID_REGEX) && targetType == UUID.class) {
            return toUUID(value, targetType);
        }

        // Check if the targetType implements BodyContent and try to parse the value as
        // JSON to that type.
        if (BodyContent.class.isAssignableFrom(targetType)) {
            return toJson(value, targetType);
        }

        if (targetType == String.class || targetType == Object.class) {
            return value;
        }

        throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getName() + ", value: " + value);
    }

    // Overloaded method to convert a string value to the specified java.lang.Number type.
    private static Object toNumber(String value, Class<?> targetType) {
        // Check if the value looks like any integer value (byte, short, int, long)
        if (value.matches(INTEGER_REGEX)) {
            return toInteger(value, targetType);
        }

        // Should be a floating point type (float, double, BigDecimal)
        return toFloatPoint(value, targetType);
    }

    // Helper methods to convert string values to specific integer types.
    private static Object toInteger(String value, Class<?> targetType) {
        if (targetType == Byte.class || targetType == byte.class)
            return Byte.valueOf(value);
        if (targetType == Short.class || targetType == short.class)
            return Short.valueOf(value);
        if (targetType == Integer.class || targetType == int.class)
            return Integer.valueOf(value);
        if (targetType == Long.class || targetType == long.class)
            return Long.valueOf(value);
        if (targetType == java.math.BigInteger.class)
            return new java.math.BigInteger(value);

        throw new IllegalArgumentException("Unsupported integer type: " + targetType.getName());
    }

    // Helper methods to convert string values to specific floating point types.
    private static Object toFloatPoint(String value, Class<?> targetType) {
        if (targetType == Float.class || targetType == float.class)
            return Float.valueOf(value);
        if (targetType == Double.class || targetType == double.class)
            return Double.valueOf(value);
        if (targetType == java.math.BigDecimal.class)
            return new java.math.BigDecimal(value);

        throw new IllegalArgumentException("Unsupported floating point type: " + targetType.getName());
    }

    // Helper method to convert string values to specific date/time types.
    private static Object toDateTime(String value, Class<?> targetType) {
        if (targetType == java.time.LocalTime.class)
            return java.time.LocalTime.parse(value);
        if (targetType == java.time.LocalDate.class)
            return java.time.LocalDate.parse(value);
        if (targetType == java.time.LocalDateTime.class)
            return java.time.LocalDateTime.parse(value);
        if (targetType == java.time.OffsetDateTime.class)
            return java.time.OffsetDateTime.parse(value);
        if (targetType == java.time.OffsetTime.class)
            return java.time.OffsetTime.parse(value);
        if (targetType == java.time.ZonedDateTime.class)
            return java.time.ZonedDateTime.parse(value);

        throw new IllegalArgumentException("Unsupported date/time type: " + targetType.getName());
    }

    // Helper method to convert string values to UUID type.
    private static Object toUUID(String value, Class<?> targetType) {
        try{
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported UUID type: " + targetType.getName(), e);
        }
    }

    /**
     * Converts a JSON string to an instance of the specified target type. The JSON is expected to contain a single key that matches the simple name of the target type (case-insensitive), and the value of that key should be an object that can be deserialized into the target type.
     * @param value the JSON string to convert
     * @param targetType the class of the target type to convert to
     * @return the converted object of the target type
     */
    private static Object toJson(String value, Class<?> targetType) {
        // Convert to a key (e.g., "Customer" -> "customer")
        String key = targetType.getSimpleName().toLowerCase();

        try {
            JsonNode rootNode = mapper.readTree(value);

            if (rootNode.has(key)) {
                // Parse only the subtree for this specific class
                return mapper.treeToValue(rootNode.get(key), targetType);
            } else {
                throw new IllegalArgumentException("JSON does not contain expected object named: " + key);
            }
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON format", e);
        }
    }

    /** 
     * Converts a list of path parameter values to the specified target types based on their positions.
     * @param pathParams the list of path parameter values (as objects)
     * @param parameterTypes the array of target parameter types corresponding to each path parameter or the parent resource result.
     * @return a list of converted objects corresponding to each path parameter and/or the parent resource result.
     * @throws IllegalArgumentException if any poblem occurs during conversion of any parameter (e.g., unsupported type, invalid format, etc.)
     */
    public static List<Object> convertPathParams(List<Object> pathParams, Class<?>[] parameterTypes) {
        return IntStream.range(0, parameterTypes.length)
                .mapToObj(i -> Convertion.convert(pathParams.get(i), parameterTypes[i]))
                .toList();
    }

    /**
     * Determines the type of the given parameter value based on its format.
     * @param value the parameter value as a string
     * @return the type description of the parameter
     */
    public static String kindOfParamType(Object object) {
        if(!(object instanceof String)) {
            return object.getClass().getName();
        }

        String value = (String) object;

        if (value.matches(INTEGER_REGEX)) {
            return "integer";
        }

        if (value.matches(FLOATING_POINT_REGEX)) {
            return "floating-point";
        }

        if (value.matches(LOCAL_TIME_REGEX)) {
            return "java.time.LocalTime";
        }

        if (value.matches(OFFSET_DATE_TIME_REGEX)) {
            return "java.time.OffsetDateTime";
        }

        if (value.matches(OFFSET_TIME_REGEX)) {
            return "java.time.OffsetTime";
        }

        if (value.matches(ZONED_DATE_TIME_REGEX)) {
            return "java.time.ZonedDateTime";
        }

        if (value.matches(UUID_REGEX)) {
            return "java.util.UUID";
        }

        if ("true".equals(value.toLowerCase().trim()) || "false".equals(value.toLowerCase().trim())) {
            return "boolean";
        }

        return "string";
    }
}
