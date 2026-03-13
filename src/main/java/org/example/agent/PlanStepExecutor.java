package org.example.agent;

import org.example.conversation.ConversationHistory;
import org.example.router.model.PlanStep;

public class PlanStepExecutor {

    private final AgentRegistry agentRegistry;

    public PlanStepExecutor(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    public AgentExecutionResult executeStep(PlanStep step, ConversationHistory history) throws Exception {
        if (step == null) {
            throw new IllegalArgumentException("Plan step cannot be null");
        }

        if (step.getAgent() == null || step.getAgent().isBlank()) {
            throw new IllegalArgumentException("Plan step agent cannot be null or blank");
        }

        SupportAgent agent = agentRegistry.getAgent(step.getAgent());
        return agent.execute(step, history);
    }
}