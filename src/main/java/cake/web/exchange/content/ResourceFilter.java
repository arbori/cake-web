package cake.web.exchange.content;

import java.lang.reflect.Method;
import java.util.Map;

public interface ResourceFilter {
    default void setFilter(ResourceFilter filter, Map<String, String[]> parameterMap) {
        Class<?> clazz = filter.getClass();

        for (var field : clazz.getDeclaredFields()) {
            String fieldName = field.getName();
            String[] queryParam = parameterMap.get(field.getName());

            if (queryParam != null) {
                String filterValue = queryParam.length > 0 ? queryParam[0] : null;
                String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

                // try setter methods first
                try {
                    for (Method m : clazz.getMethods()) {
                        if (!m.getName().equalsIgnoreCase(setterName) || m.getParameterCount() != 1) {
                            continue;
                        }

                        Class<?> paramType = m.getParameterTypes()[0];
                        Object converted = Convertion.convert(filterValue, paramType);
                        m.invoke(this, converted);

                        return;
                    }
                } catch (Exception _) {
                    // No setter found, fallback to field
                }
            }
        }
    }
}
