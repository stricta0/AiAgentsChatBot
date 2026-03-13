package org.example.agent.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class BillingSpecialistPromptDefinition {

    private static final String DEFAULT_RESOURCE_PATH = "agent/billing_specialist_prompt.json";

    private final List<String> taskLines;
    private final List<String> rules;
    private final String outputFormat;

    @JsonCreator
    public BillingSpecialistPromptDefinition(
            @JsonProperty("taskLines") List<String> taskLines,
            @JsonProperty("rules") List<String> rules,
            @JsonProperty("outputFormat") String outputFormat
    ) {
        this.taskLines = taskLines;
        this.rules = rules;
        this.outputFormat = outputFormat;
    }

    public static BillingSpecialistPromptDefinition load() {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream inputStream = getResourceAsStream(DEFAULT_RESOURCE_PATH)) {
            return objectMapper.readValue(inputStream, BillingSpecialistPromptDefinition.class);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load billing specialist prompt definition from " + DEFAULT_RESOURCE_PATH,
                    e
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

    public List<String> getTaskLines() {
        return taskLines;
    }

    public List<String> getRules() {
        return rules;
    }

    public String getOutputFormat() {
        return outputFormat;
    }
}