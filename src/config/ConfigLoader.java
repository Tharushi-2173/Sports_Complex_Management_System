
package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class ConfigLoader {
    private static final String DB_PROPERTIES_RESOURCE = "/resources/db.properties";

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream in = ConfigLoader.class.getResourceAsStream(DB_PROPERTIES_RESOURCE)) {
            if (in != null) {
                PROPERTIES.load(in);
            }
        } catch (IOException e) {
            // Fallback to environment variables if properties not present
        }
    }

    public static String getProperty(String key, String defaultValue) {
        String envValue = System.getenv(key.replace('.', '_').toUpperCase());
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static String requireProperty(String key) {
        String value = getProperty(key, null);
        return Objects.requireNonNull(value, "Missing required property: " + key);
    }
}


