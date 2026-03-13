package org.example.router.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class RouterPromptDefinition {

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
}