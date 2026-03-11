package org.example.ui.command;

import org.example.ui.ConsoleCommandDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleCommandRegistry {

    private final Map<String, ConsoleCommandHandler> handlersByName;

    public ConsoleCommandRegistry(List<ConsoleCommandHandler> handlers) {
        this.handlersByName = new HashMap<>();

        for (ConsoleCommandHandler handler : handlers) {
            handlersByName.put(handler.getName().toLowerCase(), handler);
        }
    }

    public boolean isCommand(String input, List<ConsoleCommandDefinition> configuredCommands) {
        return configuredCommands.stream()
                .map(ConsoleCommandDefinition::getName)
                .anyMatch(name -> name.equalsIgnoreCase(input));
    }

    public ConsoleCommandHandler getHandler(String input) {
        return handlersByName.get(input.toLowerCase());
    }
}