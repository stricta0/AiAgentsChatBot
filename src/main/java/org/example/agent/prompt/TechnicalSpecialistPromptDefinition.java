package org.example.agent.prompt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TechnicalSpecialistPromptDefinition {

    private final int topK;
    private final List<String> taskLines;
    private final List<String> rules;
    private final String outputFormat;

    @JsonCreator
    public TechnicalSpecialistPromptDefinition(
            @JsonProperty("topK") int topK,
            @JsonProperty("taskLines") List<String> taskLines,
            @JsonProperty("rules") List<String> rules,
            @JsonProperty("outputFormat") String outputFormat
    ) {
        this.topK = topK;
        this.taskLines = taskLines;
        this.rules = rules;
        this.outputFormat = outputFormat;
    }

    public int getTopK() {
        return topK;
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