package repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

final class DbConnectionFactory {

    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/motorph";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_CONFIG_FILE = "config/motorph-db.properties";

    private DbConnectionFactory() {
    }

    static Connection open() throws SQLException {
        Properties fileConfig = loadFileConfig();

        String url = firstNonBlank(
                System.getProperty("motorph.db.url"),
                System.getenv("MOTORPH_DB_URL"),
                fileConfig.getProperty("url"),
                DEFAULT_URL
        );

        String user = firstNonBlank(
                System.getProperty("motorph.db.user"),
                System.getenv("MOTORPH_DB_USER"),
                fileConfig.getProperty("user"),
                DEFAULT_USER
        );

        String password = firstNonBlank(
                System.getProperty("motorph.db.password"),
                System.getenv("MOTORPH_DB_PASSWORD"),
                fileConfig.getProperty("password"),
                DEFAULT_PASSWORD
        );

        return DriverManager.getConnection(url, user, password);
    }

    private static Properties loadFileConfig() {
        Properties properties = new Properties();

        Path configPath = Paths.get(DEFAULT_CONFIG_FILE);
        if (!Files.exists(configPath)) {
            return properties;
        }

        try (InputStream inputStream = Files.newInputStream(configPath)) {
            properties.load(inputStream);
        } catch (IOException ignored) {
            // Fall back to environment variables / system properties.
        }

        return properties;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}