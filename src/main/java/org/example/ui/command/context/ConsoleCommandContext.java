package org.example.ui.command.context;

import org.example.conversation.ConversationHistory;
import org.example.ui.ConsoleCommands;
import org.example.ui.ConsoleMessages;

public class ConsoleCommandContext {

    private final ConsoleMessages messages;
    private final ConsoleCommands commands;
    private final ConversationHistory history;
    private boolean exitRequested = false;

    public ConsoleCommandContext(
            ConsoleMessages messages,
            ConsoleCommands commands,
            ConversationHistory history
    ) {
        this.messages = messages;
        this.commands = commands;
        this.history = history;
    }

    public ConsoleMessages getMessages() {
        return messages;
    }

    public ConsoleCommands getCommands() {
        return commands;
    }

    public ConversationHistory getHistory() {
        return history;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    public void requestExit() {
        this.exitRequested = true;
    }
}