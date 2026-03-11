package org.example.ui.command;

import org.example.ui.command.context.ConsoleCommandContext;

public interface ConsoleCommandHandler {
    String getName();
    void execute(ConsoleCommandContext context) throws Exception;
}