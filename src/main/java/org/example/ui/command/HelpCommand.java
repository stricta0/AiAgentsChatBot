package org.example.ui.command;

import org.example.ui.ConsoleCommandDefinition;
import org.example.ui.command.context.ConsoleCommandContext;

public class HelpCommand implements ConsoleCommandHandler {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void execute(ConsoleCommandContext context) {
        System.out.println();

        for (ConsoleCommandDefinition command : context.getCommands().getCommands()) {
            System.out.printf(
                    "%s - %s (example: %s)%n",
                    command.getName(),
                    command.getDescription(),
                    command.getExample()
            );
        }

        System.out.println();
    }
}