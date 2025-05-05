package chat.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DataInputStream.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("database.properties not found");
            }
            properties.load(input);

        } catch (IOException e) {
            throw new RuntimeException("Error loading database configuration", e);
        }
    }

    public static String getUrl() {
        return properties.getProperty("db.url");
    }

    public static String getUsername() {
        return properties.getProperty("db.username");

    }

    public static String getPassword() {
        return properties.getProperty("db.password");

    }

    public static String getDriver() {
        return properties.getProperty("db.driver");

    }

}
