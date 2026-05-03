package cake.web.exchange.content;

import java.lang.reflect.Method;
import java.util.List;

public class Convertion {
    private Convertion() {
        // static class
    }

    /**
     * Converts a string value to the specified target type. It supports basic types
     * like integers, floating points, booleans, enums, date/time types, and any type 
     * that implements BodyContent (by parsing JSON).
     * @param value the string value to convert
     * @param targetType the class of the target type to convert to
     * @return the converted object of the target type
     */
    public static Object convert(String value, Class<?> targetType) {
        if (value != null) {
            try {
                // Check if the targetType implements BodyContent and try to parse the value as
                // JSON to that type.
                if (BodyContent.class.isAssignableFrom(targetType)) {
                    return ParserJson.parseJsonToObject(targetType, value);
                }

                // Check if the value looks like any integer value (byte, short, int, long)
                if (value.matches("^-?\\d+$") || value.matches("^-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?$")) {
                    return toNumber(value, targetType);
                }
                else if(value.matches("^\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?$") ||
                        value.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)$") ||
                        value.matches("^\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)$") ||
                        value.matches("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?([+-]\\d{2}:\\d{2}|Z)(\\[.*\\])?$"))
                {
                    return toDateTime(value, targetType);
                }
                else if (("true".equals(value.toLowerCase().trim()) || "false".equals(value.toLowerCase().trim())) && targetType == Boolean.class) {
                    return Boolean.valueOf(value);
                }

                return toOthers(value, targetType);
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Unsupported parameter type: " + targetType.getName() + ", value: " + value, e);
            }
        }

        return null;
    }

    // Overloaded method to convert a string value to the specified java.lang.Number type.
    private static Object toNumber(String value, Class<?> targetType) {
        // Check if the value looks like any integer value (byte, short, int, long)
        if (value.matches("^-?\\d+$")) {
            return toInteger(value, targetType);
        }
        // Check floating point types
        else if (value.matches("^-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?$")) {
            return toFloatPoint(value, targetType);
        }

        throw new IllegalArgumentException("Unsupported number type: " + targetType.getName());
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
        if (targetType == Double.class || targetType == double.class)
            return Double.valueOf(value);
        if (targetType == Float.class || targetType == float.class)
            return Float.valueOf(value);
        if (targetType == java.math.BigDecimal.class)
            return new java.math.BigDecimal(value);

        throw new IllegalArgumentException("Unsupported floating point type: " + targetType.getName());
    }

    // Helper method to convert string values to specific date/time types.
    private static Object toDateTime(String value, Class<?> targetType) {
        if (targetType == java.time.LocalTime.class)
            return java.time.LocalTime.parse(value);
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

    // Helper method to convert string values to other types like String, Enum, Character, UUID, etc.
    private static Object toOthers(String value, Class<?> targetType) {
        if (targetType == String.class || targetType == Object.class) {
            return value;
        } else if (targetType.isEnum()) {
            return convertToEnum(value, targetType);
        } else if (value.length() != 1 && (targetType == Character.class)) {
            return value.charAt(0);
        } else if (targetType == java.util.UUID.class) {
            return java.util.UUID.fromString(value);
        }

        throw new IllegalArgumentException("Unsupported type: " + targetType.getName());
    }

    // Helper method to convert string values to enum types.
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> T convertToEnum(String value, Class<?> targetType) {
        return (T) Enum.valueOf((Class<? extends Enum>) targetType.asSubclass(Enum.class), value);
    }

    public static List<Object> convertPathParams(List<String> pathParams, Class<?>[] parameterTypes) {
        List<Object> convertedArgs = new java.util.ArrayList<>();

        for (int i = 0; i < pathParams.size(); i++) {
            String paramValue = i < pathParams.size() ? pathParams.get(i) : null;
            convertedArgs.add(Convertion.convert(paramValue, parameterTypes[i]));
        }

        return convertedArgs;
    }

    /**
     * Tries to set a single attribute on the given instance by name and value.
     * It first attempts to find a setter method, then falls back to direct field
     * access.
     * 
     * @param name     the attribute name
     * @param value    the attribute value as string
     * @param clazz    the class of the instance
     * @param instance the object instance to set the attribute on
     */
    public static void trySetAttributes(String name, String value, Class<?> clazz, Object instance) {
        String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);

        // try setter methods first
        try {
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equalsIgnoreCase(setterName) || m.getParameterCount() != 1) {
                    continue;
                }

                Class<?> paramType = m.getParameterTypes()[0];
                Object converted = Convertion.convert(value, paramType);
                m.invoke(instance, converted);

                return;
            }
        } catch (Exception _) {
            // No setter found, fallback to field
        }
    }
}
