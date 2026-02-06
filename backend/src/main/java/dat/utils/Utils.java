package dat.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.http.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static String getPropertyValue(String key, String filename) {
        try (InputStream is = Utils.class.getResourceAsStream("/" + filename)) {
            if (is == null) {
                throw new RuntimeException("Could not find resource: " + filename);
            }
            Properties props = new Properties();
            props.load(is);
            return props.getProperty(key);
        } catch (IOException e) {
            throw new RuntimeException("Could not load " + filename, e);
        }
    }

    public static ObjectNode convertToJsonMessage(Context ctx, String type, String message) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", type);
        node.put("msg", message);
        return node;
    }
}
