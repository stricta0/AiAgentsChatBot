package org.example.agent;

public record AgentExecutionResult(
        AgentExecutionStatus status,
        String message
) {
}