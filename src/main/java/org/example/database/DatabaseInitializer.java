package org.example.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Objects;

public class DatabaseInitializer {

    private static final String INIT_SQL_RESOURCE_PATH = "db/init.sql";

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public DatabaseInitializer(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public void initialize() {
        String sql = loadSqlFromResources(INIT_SQL_RESOURCE_PATH);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement()) {

            statement.execute(sql);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize database", e);
        }
    }

    private String loadSqlFromResources(String resourcePath) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);

        if (Objects.isNull(inputStream)) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            return sb.toString();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to read SQL resource: " + resourcePath, e);
        }
    }
}