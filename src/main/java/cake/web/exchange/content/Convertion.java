package cake.web.exchange.content;

public class Convertion {
    private Convertion() {
        // static class
    }

    /**
     * Convert a single string to the requested target type.
     */
    public static Object convert(String value, Class<?> targetType) {
        if (value == null)
            return null;

        try {
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
            if (targetType.isEnum())
                return convertToEnum(value, targetType);
            // Add more conversions if needed (enums, dates, BigInteger, etc.)

            // If targetType is assignable from String (rare), return the raw string
            if (targetType.isAssignableFrom(String.class))
                return value;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Failed to convert value: " + value + " to type: " + targetType.getName(), e);
        }

        throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getName());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static <T> T convertToEnum(String value, Class<?> targetType) {
        return (T) Enum.valueOf((Class<? extends Enum>) targetType.asSubclass(Enum.class), value);
    }
}
