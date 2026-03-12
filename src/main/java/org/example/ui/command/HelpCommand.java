package org.example.ui.command;

import org.example.ui.ConsoleCommandDefinition;
import org.example.ui.command.context.ConsoleCommandContext;

public class HelpCommand implements ConsoleCommandHandler {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String execute(ConsoleCommandContext context) {
        StringBuilder sb = new StringBuilder();

        for (ConsoleCommandDefinition command : context.getCommands().getCommands()) {
            sb.append(command.getName())
                    .append(" - ")
                    .append(command.getDescription())
                    .append(" (example: ")
                    .append(command.getExample())
                    .append(")")
                    .append("\n");
        }

        return sb.toString().trim();
    }
}