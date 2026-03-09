package dat.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.http.Context;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    /**
     * Tries to read a key from a .env file in the current working directory or its parent.
     * Lines are "KEY=value"; leading/trailing whitespace and quotes are trimmed from value.
     */
    public static String getEnvFromDotEnv(String key) {
        Path cwd = Path.of(System.getProperty("user.dir"));
        Path parent = cwd.getParent();
        for (Path base : new Path[]{cwd, parent != null ? parent : cwd}) {
            Path envPath = base.resolve(".env").normalize();
            if (!Files.isRegularFile(envPath)) continue;
            try {
                for (String line : Files.readAllLines(envPath)) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int eq = line.indexOf('=');
                    if (eq <= 0) continue;
                    String k = line.substring(0, eq).trim();
                    if (!key.equals(k)) continue;
                    String v = line.substring(eq + 1).trim();
                    if (v.length() >= 2 && (v.startsWith("\"") && v.endsWith("\"") || v.startsWith("'") && v.endsWith("'")))
                        v = v.substring(1, v.length() - 1);
                    return v;
                }
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    public static ObjectNode convertToJsonMessage(Context ctx, String type, String message) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", type);
        node.put("msg", message);
        return node;
    }
}
