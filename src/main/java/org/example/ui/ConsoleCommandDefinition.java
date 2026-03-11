package org.example.ui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsoleCommandDefinition {

    private final String name;
    private final String description;
    private final String example;

    @JsonCreator
    public ConsoleCommandDefinition(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("example") String example
    ) {
        this.name = name;
        this.description = description;
        this.example = example;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getExample() {
        return example;
    }
}