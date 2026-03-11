package org.example.router.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class AgentCatalog {

    private static final String DEFAULT_RESOURCE_PATH = "router/agents.json";

    private final List<AgentDefinition> agents;

    @JsonCreator
    public AgentCatalog(@JsonProperty("agents") List<AgentDefinition> agents) {
        this.agents = agents;
    }

    public List<AgentDefinition> getAgents() {
        return agents;
    }

    /**
     * Loads agent catalog from resources/router/agents.json
     */
    public static AgentCatalog load() {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream inputStream = getResourceAsStream(DEFAULT_RESOURCE_PATH)) {
            return objectMapper.readValue(inputStream, AgentCatalog.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load agent catalog from " + DEFAULT_RESOURCE_PATH, e);
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