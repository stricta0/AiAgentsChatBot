package org.example.agent;

public class PromptExecutionAbortException extends RuntimeException {

    public PromptExecutionAbortException(String message) {
        super(message);
    }
}