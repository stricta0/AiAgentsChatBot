package org.example.conversation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversationHistory {

    private final List<ConversationEntry> entries;

    public ConversationHistory() {
        this.entries = new ArrayList<>();
    }

    public void addUserMessage(String message) {
        entries.add(new ConversationEntry(ConversationEntry.Role.USER, message));
    }

    public void addAssistantMessage(String message) {
        entries.add(new ConversationEntry(ConversationEntry.Role.ASSISTANT, message));
    }

    public List<ConversationEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public List<ConversationEntry> getEntriesWithoutLast() {
        if (entries.isEmpty()) {
            return List.of();
        }

        return Collections.unmodifiableList(entries.subList(0, entries.size() - 1));
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public boolean hasAtLeastTwoEntries() {
        return entries.size() >= 2;
    }

    public String toPromptString() {
        return toPromptString(entries);
    }

    public String toPromptStringWithoutLast() {
        return toPromptString(getEntriesWithoutLast());
    }

    private String toPromptString(List<ConversationEntry> entriesToRender) {
        StringBuilder sb = new StringBuilder();

        for (ConversationEntry entry : entriesToRender) {
            sb.append(entry.getRole().name())
                    .append(": ")
                    .append(entry.getContent())
                    .append("\n");
        }

        return sb.toString().trim();
    }

    public String toPrettyString() {
        if (entries.isEmpty()) {
            return "Conversation history is empty.";
        }

        StringBuilder sb = new StringBuilder();

        for (ConversationEntry entry : entries) {
            sb.append(entry.getRole().name())
                    .append(": ")
                    .append(entry.getContent())
                    .append("\n\n");
        }

        return sb.toString().trim();
    }
}