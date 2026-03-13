package org.example.ui.command;

import org.example.ui.ConsoleCommandDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleCommandRegistry {

    private final Map<String, ConsoleCommandHandler> handlersByName;

    public ConsoleCommandRegistry(
            List<ConsoleCommandHandler> handlers,
            List<ConsoleCommandDefinition> configuredCommands
    ) {
        this.handlersByName = new HashMap<>();

        for (ConsoleCommandHandler handler : handlers) {
            String handlerName = normalize(handler.getName());

            if (handlerName.isBlank()) {
                throw new IllegalStateException("Console command handler name cannot be null or blank");
            }

            if (handlersByName.containsKey(handlerName)) {
                throw new IllegalStateException(
                        "Duplicate console command handler implementation for command: " + handler.getName()
                );
            }

            handlersByName.put(handlerName, handler);
        }

        validateConfiguredCommands(configuredCommands);
        validateRegisteredHandlers(configuredCommands);
    }

    public boolean isCommand(String input, List<ConsoleCommandDefinition> configuredCommands) {
        return configuredCommands.stream()
                .map(ConsoleCommandDefinition::getName)
                .anyMatch(name -> name.equalsIgnoreCase(input));
    }

    public ConsoleCommandHandler getHandler(String input) {
        return handlersByName.get(normalize(input));
    }

    private void validateConfiguredCommands(List<ConsoleCommandDefinition> configuredCommands) {
        for (ConsoleCommandDefinition commandDefinition : configuredCommands) {
            String commandName = normalize(commandDefinition.getName());

            if (commandName.isBlank()) {
                throw new IllegalStateException("Command name in console_commands.json cannot be null or blank");
            }

            if (!handlersByName.containsKey(commandName)) {
                throw new IllegalStateException(
                        "Missing runtime implementation for command defined in console_commands.json: "
                                + commandDefinition.getName()
                );
            }
        }
    }

    private void validateRegisteredHandlers(List<ConsoleCommandDefinition> configuredCommands) {
        Map<String, Boolean> configuredCommandNames = new HashMap<>();

        for (ConsoleCommandDefinition commandDefinition : configuredCommands) {
            configuredCommandNames.put(normalize(commandDefinition.getName()), true);
        }

        for (String handlerName : handlersByName.keySet()) {
            if (!configuredCommandNames.containsKey(handlerName)) {
                throw new IllegalStateException(
                        "Missing command definition in console_commands.json for runtime handler: " + handlerName
                );
            }
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}