package org.example.ui.command.impl;

import org.example.ui.command.ConsoleCommandHandler;
import org.example.ui.command.context.ConsoleCommandContext;

public class ExitCommand implements ConsoleCommandHandler {

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String execute(ConsoleCommandContext context) {
        context.requestExit();
        return context.getMessages().getGoodbyeMessage();
    }
}