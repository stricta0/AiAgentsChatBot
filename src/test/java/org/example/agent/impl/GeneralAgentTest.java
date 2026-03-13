package org.example.agent.impl;

import org.example.agent.AgentExecutionResult;
import org.example.agent.AgentExecutionStatus;
import org.example.conversation.ConversationHistory;
import org.example.llm.LlmClient;
import org.example.router.model.PlanStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class GeneralAgentTest {

    private LlmClient llmClient;
    private GeneralAgent generalAgent;

    @BeforeEach
    void setUp() {
        llmClient = mock(LlmClient.class);
        generalAgent = new GeneralAgent(llmClient);
    }

    @Test
    void shouldReturnSuccessWithLlmResponse() throws Exception {
        PlanStep step = new PlanStep();
        step.setAgent("GENERAL");
        step.setTask("Acknowledge the user and provide general assistance.");
        step.setOriginalMessageSection("hello");
        step.setAdditionalContext("");
        step.setConfidence(1.0);

        ConversationHistory history = new ConversationHistory();

        when(llmClient.getResponseFromGemini(anyString(), eq(history)))
                .thenReturn("Hello! How can I help you today?");

        AgentExecutionResult result = generalAgent.execute(step, history);

        assertEquals(AgentExecutionStatus.SUCCESS, result.status());
        assertEquals("Hello! How can I help you today?", result.message());
    }

    @Test
    void shouldThrowWhenLlmReturnsBlankResponse() throws Exception {
        PlanStep step = new PlanStep();
        step.setAgent("GENERAL");
        step.setTask("Respond generally");
        step.setOriginalMessageSection("hello");
        step.setAdditionalContext("");
        step.setConfidence(1.0);

        ConversationHistory history = new ConversationHistory();

        when(llmClient.getResponseFromGemini(anyString(), eq(history)))
                .thenReturn("   ");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> generalAgent.execute(step, history)
        );

        assertEquals("General agent returned empty response", exception.getMessage());
    }

    @Test
    void shouldExposeGeneralName() {
        assertEquals("GENERAL", generalAgent.getName());
    }
}