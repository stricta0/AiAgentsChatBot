package org.example.ui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ConsoleCommands {

    private final List<ConsoleCommandDefinition> commands;

    @JsonCreator
    public ConsoleCommands(
            @JsonProperty("commands") List<ConsoleCommandDefinition> commands
    ) {
        this.commands = commands;
    }

    public List<ConsoleCommandDefinition> getCommands() {
        return commands;
    }
}