package org.example.database;

import org.example.config.EnvConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresContainerManager {

    private final PostgreSQLContainer<?> postgresContainer;

    public PostgresContainerManager() {

        String imageName = EnvConfig.getPostgresImage();
        String database = EnvConfig.getPostgresDb();
        String username = EnvConfig.getPostgresUser();
        String password = EnvConfig.getPostgresPassword();

        this.postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse(imageName))
                .withDatabaseName(database)
                .withUsername(username)
                .withPassword(password);
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