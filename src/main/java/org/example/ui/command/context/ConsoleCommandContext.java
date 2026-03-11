package org.example.ui.command.context;

import org.example.ui.ConsoleCommands;
import org.example.ui.ConsoleMessages;

public class ConsoleCommandContext {

    private final ConsoleMessages messages;
    private final ConsoleCommands commands;

    private boolean exitRequested = false;

    public ConsoleCommandContext(ConsoleMessages messages, ConsoleCommands commands) {
        this.messages = messages;
        this.commands = commands;
    }

    public ConsoleMessages getMessages() {
        return messages;
    }

    public ConsoleCommands getCommands() {
        return commands;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    public void requestExit() {
        this.exitRequested = true;
    }
}