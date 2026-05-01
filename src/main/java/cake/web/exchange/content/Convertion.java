package cake.web.exchange.content;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Convertion {
    private Convertion() {
        // static class
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

    /**
     * Convert a single string to the requested target type.
     */
    public static Object convert(String value, Class<?> targetType) {
        if (value != null) {
            try {
                // Check if the targetType implements BodyContent and try to parse the value as JSON to that type.
                if (BodyContent.class.isAssignableFrom(targetType)) {
                    return ParserJson.parseJsonToObject(targetType, value);
                }

                if (targetType == String.class)
                    return value;
                if (targetType == Integer.class || targetType == int.class)
                    return Integer.valueOf(value);
                if (targetType == Long.class || targetType == long.class)
                    return Long.valueOf(value);
                if (targetType == Boolean.class || targetType == boolean.class)
                    return Boolean.valueOf(value);
                if (targetType == Double.class || targetType == double.class)
                    return Double.valueOf(value);
                if (targetType == Float.class || targetType == float.class)
                    return Float.valueOf(value);
                if (targetType == java.math.BigDecimal.class) 
                    return new BigDecimal(value);
                if (targetType == java.time.LocalDate.class) 
                    return LocalDate.parse(value);
                if (targetType.isEnum())
                    return convertToEnum(value, targetType);
                if (targetType == Character.class || targetType == char.class) {
                    if (value.length() != 1) {
                        throw new IllegalArgumentException("Cannot convert to char: " + value);
                    }
                    return value.charAt(0);
                }
                if (targetType == Byte.class || targetType == byte.class)
                    return Byte.valueOf(value);
                if (targetType == Short.class || targetType == short.class)
                    return Short.valueOf(value);
                if (targetType == java.time.LocalDate.class)
                    return java.time.LocalDate.parse(value);
                if (targetType == java.time.LocalDateTime.class)
                    return java.time.LocalDateTime.parse(value);
                if (targetType == java.time.LocalTime.class)
                    return java.time.LocalTime.parse(value);
                if (targetType == java.time.OffsetDateTime.class)
                    return java.time.OffsetDateTime.parse(value);
                if (targetType == java.time.OffsetTime.class)
                    return java.time.OffsetTime.parse(value);
                if (targetType == java.time.ZonedDateTime.class)
                    return java.time.ZonedDateTime.parse(value);
                if (targetType == java.util.UUID.class)
                    return java.util.UUID.fromString(value);
                if (targetType == Object.class)
                    return value;

                // If targetType is assignable from String (rare), return the raw string
                if (targetType.isAssignableFrom(String.class))
                    return value;
            } catch (Exception e) {
                throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getName() + ", value: " + value, e);
            }
        }

        return null;
    }

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
}
