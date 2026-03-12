package org.example.ui.command;

import org.example.ui.command.context.ConsoleCommandContext;

public class HistoryCommand implements ConsoleCommandHandler {

    @Override
    public String getName() {
        return "history";
    }

    @Override
    public String execute(ConsoleCommandContext context) {
        return context.getHistory().toPrettyString();
    }
}