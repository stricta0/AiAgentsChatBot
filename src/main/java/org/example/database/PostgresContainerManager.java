package org.example.database;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresContainerManager {

    private static final String IMAGE_NAME = "postgres:16";
    private static final String DATABASE_NAME = "aiagents";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";

    private final PostgreSQLContainer<?> postgresContainer;

    public PostgresContainerManager() {
        this.postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse(IMAGE_NAME))
                .withDatabaseName(DATABASE_NAME)
                .withUsername(USERNAME)
                .withPassword(PASSWORD);
    }

    public void start() {
        postgresContainer.start();
    }

    public void stop() {
        postgresContainer.stop();
    }

    public String getJdbcUrl() {
        return postgresContainer.getJdbcUrl();
    }

    public String getUsername() {
        return postgresContainer.getUsername();
    }

    public String getPassword() {
        return postgresContainer.getPassword();
    }
}