package org.example.agent;

import org.example.conversation.ConversationHistory;
import org.example.router.model.PlanStep;

public interface SupportAgent {

    String getName();

    AgentExecutionResult execute(PlanStep step, ConversationHistory history) throws Exception;
}