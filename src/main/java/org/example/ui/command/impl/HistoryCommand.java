package org.example.ui.command.impl;

import org.example.ui.command.ConsoleCommandHandler;
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