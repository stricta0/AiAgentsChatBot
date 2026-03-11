package org.example.router.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class RouterPromptDefinition {

    private static final String DEFAULT_RESOURCE_PATH = "router/router_prompt.json";

    private final List<String> task;
    private final List<String> rules;

    @JsonCreator
    public RouterPromptDefinition(
            @JsonProperty("taskLines") List<String> task,
            @JsonProperty("rules") List<String> rules
    ) {
        this.task = task;
        this.rules = rules;
    }

    public List<String> getTask() {
        return task;
    }

    public List<String> getRules() {
        return rules;
    }

    public static RouterPromptDefinition load() {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream inputStream = getResourceAsStream(DEFAULT_RESOURCE_PATH)) {
            return objectMapper.readValue(inputStream, RouterPromptDefinition.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load router prompt definition from " + DEFAULT_RESOURCE_PATH, e);
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
}