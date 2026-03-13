package org.example.agent.impl;

import org.example.agent.AgentExecutionResult;
import org.example.agent.AgentExecutionStatus;
import org.example.agent.SupportAgent;
import org.example.conversation.ConversationHistory;
import org.example.llm.LlmClient;
import org.example.router.model.PlanStep;

public class GeneralAgent implements SupportAgent {

    private final LlmClient llmClient;

    public GeneralAgent(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public String getName() {
        return "GENERAL";
    }

    @Override
    public AgentExecutionResult execute(PlanStep step, ConversationHistory history) throws Exception {
        String prompt = buildPrompt(step, history);
        String response = llmClient.getResponseFromGemini(prompt, history);

        if (response == null || response.isBlank()) {
            throw new IllegalStateException("General agent returned empty response");
        }

        return new AgentExecutionResult(
                AgentExecutionStatus.SUCCESS,
                response.trim()
        );
    }

    private String buildPrompt(PlanStep step, ConversationHistory history) {
        String task = safe(step.getTask());
        String originalMessageSection = safe(step.getOriginalMessageSection());
        String additionalContext = safe(step.getAdditionalContext());

        return """
                You are a general-purpose assistant handling a GENERAL step in a multi-agent support conversation.

                Respond naturally and helpfully.
                Keep the response relevant to the user's current message.
                Do not pretend to have used billing tools or technical documentation unless that actually happened.
                If the user is only greeting you, greet them and offer help.

                CURRENT STEP
                agent: %s
                task: %s
                original_message_section: %s
                additional_context: %s
                """.formatted(
                safe(step.getAgent()),
                task,
                originalMessageSection,
                additionalContext
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}