package cake.web.exchange.content;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParserJson {
    private ParserJson() {
        // static class
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a simple JSON string and sets the corresponding attributes on the
     * given instance.
     * Assumes JSON is in the format {"key1":"value1","key2":"value2",...}
     * 
     * @param instance the object instance to set attributes on
     * @param json     the JSON string to parse
     * @throws IOException if parsing fails
     */
    public static void parseJsonToObject(Object instance, String json) throws IOException {
        if (instance == null || json == null || json.isBlank()) {
            return;
        }

        try {
            // Update the existing object in-place
            objectMapper.readerForUpdating(instance)
                    .readValue(json);
        } catch (IOException e) {
            throw new IOException("Failed to map JSON into instance of "
                    + instance.getClass().getName() + ": " + e.getMessage(), e);
        }
    }
}
