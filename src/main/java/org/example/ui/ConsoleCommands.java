package org.example.ui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class ConsoleCommands {

    private static final String DEFAULT_RESOURCE_PATH = "ui/console_commands.json";

    private final List<ConsoleCommandDefinition> commands;

    @JsonCreator
    public ConsoleCommands(
            @JsonProperty("commands") List<ConsoleCommandDefinition> commands
    ) {
        this.commands = commands;
    }

    public static ConsoleCommands load() {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream inputStream = getResourceAsStream(DEFAULT_RESOURCE_PATH)) {
            return objectMapper.readValue(inputStream, ConsoleCommands.class);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load console commands from " + DEFAULT_RESOURCE_PATH, e
            );
        }
    }

    private static InputStream getResourceAsStream(String resourcePath) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(resourcePath);

        if (Objects.isNull(inputStream)) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }

        return inputStream;
    }

    public List<ConsoleCommandDefinition> getCommands() {
        return commands;
    }
}