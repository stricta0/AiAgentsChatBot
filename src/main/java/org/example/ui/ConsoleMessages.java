package org.example.ui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ConsoleMessages {

    private static final String DEFAULT_RESOURCE_PATH = "ui/console_messages.json";

    private final String welcomeMessage;
    private final String helpMessage;
    private final String prompt;
    private final String emptyMessageWarning;
    private final String goodbyeMessage;
    private final String routingPlanHeader;
    private final String errorPrefix;

    @JsonCreator
    public ConsoleMessages(
            @JsonProperty("welcomeMessage") String welcomeMessage,
            @JsonProperty("helpMessage") String helpMessage,
            @JsonProperty("prompt") String prompt,
            @JsonProperty("emptyMessageWarning") String emptyMessageWarning,
            @JsonProperty("goodbyeMessage") String goodbyeMessage,
            @JsonProperty("routingPlanHeader") String routingPlanHeader,
            @JsonProperty("errorPrefix") String errorPrefix
    ) {
        this.welcomeMessage = welcomeMessage;
        this.helpMessage = helpMessage;
        this.prompt = prompt;
        this.emptyMessageWarning = emptyMessageWarning;
        this.goodbyeMessage = goodbyeMessage;
        this.routingPlanHeader = routingPlanHeader;
        this.errorPrefix = errorPrefix;
    }

    public static ConsoleMessages load() {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream inputStream = getResourceAsStream(DEFAULT_RESOURCE_PATH)) {
            return objectMapper.readValue(inputStream, ConsoleMessages.class);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load console messages from " + DEFAULT_RESOURCE_PATH, e
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

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public String getHelpMessage() {
        return helpMessage;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getEmptyMessageWarning() {
        return emptyMessageWarning;
    }

    public String getGoodbyeMessage() {
        return goodbyeMessage;
    }

    public String getRoutingPlanHeader() {
        return routingPlanHeader;
    }

    public String getErrorPrefix() {
        return errorPrefix;
    }
}