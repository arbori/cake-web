package cake.web.exchange.content;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ParserJson {
    private ParserJson() {
        // static class
    }   

    /**
     * Parses a simple JSON string and sets the corresponding attributes on the given instance.
     * Assumes JSON is in the format {"key1":"value1","key2":"value2",...}
     * 
     * @param instance the object instance to set attributes on
     * @param json the JSON string to parse
     */
    public static Object parseJsonToObject(Object instance, String json) {
        if(instance == null || json == null || json.isEmpty()) {
            return instance;
        }

        try {
            Class<?> targetType = instance.getClass();
            json = json.trim();

            if (json.startsWith("{") && json.endsWith("}")) {
                json = json.substring(1, json.length() - 1).trim();
            }

            // Handle empty object
            if (json.isEmpty()) {
                return instance;
            }

            // Split respecting nested braces
            for (String pair : splitJsonPairs(json)) {
                String[] kv = pair.split(":", 2);
                if (kv.length != 2) continue;

                String key = kv[0].trim().replaceAll("(^\")|(\"$)", "");
                String value = kv[1].trim();

                if (value.startsWith("{")) {
                    // Nested object
                    Object field = targetType.getDeclaredField(key);
                    
                    Object nested = parseJsonToObject(field, value);
                    
                    trySetAttributes(key, nested, targetType, instance);
                } else {
                    // Simple value
                    String cleanValue = value.replaceAll("(^\\\")|(\\\"$)", "");
                    trySetAttributes(key, cleanValue, targetType, instance);
                }
            }

            return instance;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse JSON into " + instance.getClass().getSimpleName(), e);
        }
    }

    /**
     * Splits a JSON object string into key:value pairs respecting nested braces.
     */
    private static List<String> splitJsonPairs(String json) {
        List<String> pairs = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        Deque<Character> braces = new ArrayDeque<>();
        boolean inQuotes = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{') braces.push('{');
                else if (c == '}') braces.pop();
                else if (c == ',' && braces.isEmpty()) {
                    pairs.add(current.toString());
                    current.setLength(0);
                    continue;
                }
            }

            current.append(c);
        }

        if (!current.isEmpty()) {
            pairs.add(current.toString());
        }

        return pairs.stream().map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    /**
     * Enhanced setter utility that supports both raw and object value injection.
     */
    private static void trySetAttributes(String name, Object value, Class<?> clazz, Object instance) {
        String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        try {
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equalsIgnoreCase(setterName) || m.getParameterCount() != 1)
                    continue;

                Class<?> paramType = m.getParameterTypes()[0];
                Object finalValue = value;

                if (value instanceof String strVal) {
                    finalValue = Convertion.convert(strVal, paramType);
                }

                m.invoke(instance, finalValue);
                return;
            }
        } catch (Exception _) {
            // fallback to field direct set
        }
    }
}