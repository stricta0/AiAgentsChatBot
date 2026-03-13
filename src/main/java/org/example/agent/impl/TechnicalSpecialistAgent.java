package org.example.agent.impl;

import org.example.agent.AgentExecutionResult;
import org.example.agent.AgentExecutionStatus;
import org.example.agent.SupportAgent;
import org.example.conversation.ConversationHistory;
import org.example.router.model.PlanStep;

public class TechnicalSpecialistAgent implements SupportAgent {

    @Override
    public String getName() {
        return "TECHNICAL_SPECIALIST";
    }

    @Override
    public AgentExecutionResult execute(PlanStep step, ConversationHistory history) {
        return new AgentExecutionResult(
                AgentExecutionStatus.CANNOT_HANDLE,
                "TechnicalSpecialistAgent is not implemented yet."
        );
    }
}