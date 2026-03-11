package org.example.ui.command;

import org.example.ui.command.context.ConsoleCommandContext;

public class ExitCommand implements ConsoleCommandHandler {

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public void execute(ConsoleCommandContext context) {
        System.out.println(context.getMessages().getGoodbyeMessage());
        context.requestExit();
    }
}