package org.example.conversation;

public class ConversationEntry {

    public enum Role {
        USER,
        ASSISTANT
    }

    private final Role role;
    private final String content;

    public ConversationEntry(Role role, String content) {
        this.role = role;
        this.content = content;
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}