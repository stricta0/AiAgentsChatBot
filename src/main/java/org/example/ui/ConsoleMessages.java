package org.example.ui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsoleMessages {

    private final String welcomeMessage;
    private final String helpMessage;
    private final String prompt;
    private final String emptyMessageWarning;
    private final String goodbyeMessage;
    private final String routingPlanHeader;
    private final String resolvedRoutingPlanHeader;
    private final String unknownPlanDetectedMessage;
    private final String unknownPlanOptionsMessage;
    private final String unknownPlanPrompt;
    private final String errorPrefix;
    private final String newPromptMessage;

    @JsonCreator
    public ConsoleMessages(
            @JsonProperty("welcomeMessage") String welcomeMessage,
            @JsonProperty("helpMessage") String helpMessage,
            @JsonProperty("prompt") String prompt,
            @JsonProperty("emptyMessageWarning") String emptyMessageWarning,
            @JsonProperty("goodbyeMessage") String goodbyeMessage,
            @JsonProperty("routingPlanHeader") String routingPlanHeader,
            @JsonProperty("resolvedRoutingPlanHeader") String resolvedRoutingPlanHeader,
            @JsonProperty("unknownPlanDetectedMessage") String unknownPlanDetectedMessage,
            @JsonProperty("unknownPlanOptionsMessage") String unknownPlanOptionsMessage,
            @JsonProperty("unknownPlanPrompt") String unknownPlanPrompt,
            @JsonProperty("errorPrefix") String errorPrefix,
            @JsonProperty("newPromptMessage") String newPromptMessage
    ) {
        this.welcomeMessage = welcomeMessage;
        this.helpMessage = helpMessage;
        this.prompt = prompt;
        this.emptyMessageWarning = emptyMessageWarning;
        this.goodbyeMessage = goodbyeMessage;
        this.routingPlanHeader = routingPlanHeader;
        this.resolvedRoutingPlanHeader = resolvedRoutingPlanHeader;
        this.unknownPlanDetectedMessage = unknownPlanDetectedMessage;
        this.unknownPlanOptionsMessage = unknownPlanOptionsMessage;
        this.unknownPlanPrompt = unknownPlanPrompt;
        this.errorPrefix = errorPrefix;
        this.newPromptMessage = newPromptMessage;
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

    public String getResolvedRoutingPlanHeader() {
        return resolvedRoutingPlanHeader;
    }

    public String getUnknownPlanDetectedMessage() {
        return unknownPlanDetectedMessage;
    }

    public String getUnknownPlanOptionsMessage() {
        return unknownPlanOptionsMessage;
    }

    public String getUnknownPlanPrompt() {
        return unknownPlanPrompt;
    }

    public String getErrorPrefix() {
        return errorPrefix;
    }

    public String getNewPromptMessage() {
        return newPromptMessage;
    }
}