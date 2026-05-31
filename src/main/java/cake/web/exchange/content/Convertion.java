package cake.web.exchange.content;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cake.web.exception.PrimitiveNotAllowedException;

/**
 * Utility class for converting string values to various target types based on 
 * their format. This class provides methods to convert path parameters, query 
 * parameters, and other string inputs to their corresponding Java types such 
 * as numeric types, date/time types, boolean, UUID, etc. The conversion logic 
 * is based on regular expressions that match the expected formats of the input 
 * values.
 * 
 * There are colateral behavior under the decision to convert string values 
 * based on how string looks like. The format are agruped folow the content
 * in integers, floating point, date/time, boolean, UUID, and string types.
 * Then, is a content looks like an integer, the conversion will be made 
 * only for integers type (byte, short, int, long, BigInteger), even it is 
 * possible to convert it to a floating point type (float, double, BigDecimal).
 * The same applies for the other types. For example, if a content looks like
 * a date/time string, the conversion will be made only for date/time types,
 * even it is possible to convert it to a string type.
 * 
 * The consequence is if the frame work is trying convert a content for a 
 * Float parameter, but the content looks like an integer (e.g., "123"), 
 * the conversion will fail and the framework can throw an IlegalArgumentException
 * or NoSuchMethodException, depending on the context of the conversion 
 * (e.g., if it is trying to convert a query parameter, path parameter, etc.).
 */
public class Convertion {
    private Convertion() {
        // static class
    }

    private static final String INTEGER_REGEX = "^-?\\d+$";
    private static final String FLOATING_POINT_REGEX = "^-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?$";
    private static final String LOCAL_TIME_REGEX = "^\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?$";
    private static final String LOCAL_DATE_REGEX = "^\\d{4}-\\d{2}-\\d{2}$";
    private static final String LOCAL_DATE_TIME_REGEX = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?$";
    private static final String OFFSET_DATE_TIME_REGEX = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)$";
    private static final String OFFSET_TIME_REGEX = "^\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)$";
    private static final String ZONED_DATE_TIME_REGEX = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)(\\[.*\\])?$";
    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    /**
     * Converts the given object to the specified target type. The conversion logic is based on the format of the input value and the target type.
     * Supported conversions include:
     * <ul>
     *   <li>Numeric types (byte, short, int, long, float, double, BigInteger, BigDecimal)</li>
     *   <li>Date/time types (LocalTime, LocalDate, LocalDateTime, OffsetDateTime, OffsetTime, ZonedDateTime)</li>
     *   <li>Boolean type</li>
     *   <li>UUID type</li>
     *   <li>String and Object types (returns the string value)</li>
     * </ul>
     *
     * @param object the input object to convert (typically a string representation of a path parameter)
     * @param targetType the class of the target type to convert to
     * @return the converted object of the target type
     * @throws ClassCastException if the conversion cannot be performed due to unsupported types or invalid formats
     */
    public static Object convert(Object object, Class<?> targetType) {
        if (object == null) {
            return null;
        }

        if(targetType.isInstance(object)) {
            return object;
        }

        String value = object.toString();
        Object result = null;

        // Check if the value looks like any integer value (byte, short, int, long)
        if (value.matches(INTEGER_REGEX) || value.matches(FLOATING_POINT_REGEX)) {
            result = toNumber(value, targetType);
        }
        
        // Check if the value looks like a date/time string (e.g., "2023-08-15T14:30:00Z", "14:30:00", "2023-08-15T14:30:00", etc.)
        else if(value.matches(LOCAL_TIME_REGEX) ||
            value.matches(LOCAL_DATE_REGEX) ||
            value.matches(LOCAL_DATE_TIME_REGEX) ||
            value.matches(OFFSET_DATE_TIME_REGEX) ||
            value.matches(OFFSET_TIME_REGEX) ||
            value.matches(ZONED_DATE_TIME_REGEX))
        {
            result = toDateTime(value, targetType);
        }

        else if (("true".equals(value.toLowerCase().trim()) || "false".equals(value.toLowerCase().trim())) && targetType == Boolean.class) {
            result = Boolean.valueOf(value.toLowerCase().trim());
        }

        else if(value.matches(UUID_REGEX) && targetType == UUID.class) {
            result = toUUID(value);
        }

        if(result != null) {
            return result;
        } else if (targetType == String.class || targetType == Object.class) {
            return value;
        }

        throw new ClassCastException("Unmatch parameter type and parameter data: " + targetType.getName() + ", value: " + value);
    }

    // Method to convert a string value to the specified java.lang.Number type.
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
        if (targetType == Byte.class)
            return Byte.valueOf(value);
        if (targetType == Short.class)
            return Short.valueOf(value);
        if (targetType == Integer.class)
            return Integer.valueOf(value);
        if (targetType == Long.class)
            return Long.valueOf(value);
        if (targetType == java.math.BigInteger.class)
            return new java.math.BigInteger(value);

        return null;
    }

    // Helper methods to convert string values to specific floating point types.
    private static Object toFloatPoint(String value, Class<?> targetType) {
        if (targetType == Float.class)
            return Float.valueOf(value);
        if (targetType == Double.class)
            return Double.valueOf(value);
        if (targetType == java.math.BigDecimal.class)
            return new java.math.BigDecimal(value);

        return null;
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

        return null;
    }

    // Helper method to convert string values to UUID type.
    private static Object toUUID(String value) {
        try{
            return UUID.fromString(value);
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

    /** 
     * Converts a list of path parameter values to the specified target types based on their positions.
     * @param pathParams the list of path parameter values (as objects)
     * @param parameterTypes the array of target parameter types corresponding to each path parameter or the parent resource result.
     * @return a list of converted objects corresponding to each path parameter and/or the parent resource result.
     * @throws ArrayIndexOutOfBoundsException if the number of path parameters is different from the number of parameter types.
     * @throws PrimitiveNotAllowedException if a parameter type is a primitive type.
     */
    public static List<Object> convertPathParams(List<Object> pathParams, Class<?>[] parameterTypes) {
        if(pathParams.size() != parameterTypes.length) {
            throw new ArrayIndexOutOfBoundsException("The number of path parameters (" + pathParams.size() + ") is different of the number of parameter types (" + parameterTypes.length + ").");
        }

        List<Object> result = new ArrayList<>(parameterTypes.length);

        for(int i = 0; i < parameterTypes.length; i++) {
            if(parameterTypes[i].isPrimitive()) {
                throw new PrimitiveNotAllowedException("Parameter with type " + parameterTypes[i].getName() + " is not allowed");
            }

            result.add(convert(pathParams.get(i), parameterTypes[i]));
        }

        return result;
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
