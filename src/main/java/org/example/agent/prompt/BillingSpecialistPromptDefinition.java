package org.example.agent.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BillingSpecialistPromptDefinition {

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