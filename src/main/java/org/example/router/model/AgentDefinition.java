package org.example.router.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AgentDefinition {

    private final String name;
    private final String description;
    private final List<String> canHandle;
    private final List<String> cannotHandle;
    private final List<String> examples;

    @JsonCreator
    public AgentDefinition(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("canHandle") List<String> canHandle,
            @JsonProperty("cannotHandle") List<String> cannotHandle,
            @JsonProperty("examples") List<String> examples
    ) {
        this.name = name;
        this.description = description;
        this.canHandle = canHandle;
        this.cannotHandle = cannotHandle;
        this.examples = examples;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getCanHandle() {
        return canHandle;
    }

    public List<String> getCannotHandle() {
        return cannotHandle;
    }

    public List<String> getExamples() {
        return examples;
    }
}