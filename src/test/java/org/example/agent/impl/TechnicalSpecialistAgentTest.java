package org.example.agent.impl;

import org.example.agent.AgentExecutionResult;
import org.example.agent.AgentExecutionStatus;
import org.example.agent.PromptExecutionAbortException;
import org.example.conversation.ConversationHistory;
import org.example.llm.LlmClient;
import org.example.router.model.PlanStep;
import org.example.technicaldocs.TechnicalDocumentationService;
import org.example.technicaldocs.model.TechnicalDocumentChunk;
import org.example.technicaldocs.model.TechnicalSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class TechnicalSpecialistAgentTest {

    private LlmClient llmClient;
    private TechnicalDocumentationService technicalDocumentationService;
    private TechnicalSpecialistAgent technicalSpecialistAgent;

    @BeforeEach
    void setUp() {
        llmClient = mock(LlmClient.class);
        technicalDocumentationService = mock(TechnicalDocumentationService.class);

        technicalSpecialistAgent = new TechnicalSpecialistAgent(
                llmClient,
                technicalDocumentationService
        );
    }

    @Test
    void shouldReturnSuccessWhenDocumentationCoversQuestion() throws Exception {
        PlanStep step = buildTechnicalStep(
                "Explain webhook configuration",
                "How do I configure webhooks?",
                ""
        );

        ConversationHistory history = new ConversationHistory();

        List<TechnicalSearchResult> searchResults = List.of(
                new TechnicalSearchResult(
                        new TechnicalDocumentChunk(
                                "integration.md",
                                ".md",
                                "Integration Guide > Webhook Configuration",
                                1,
                                "Heading Path: Integration Guide > Webhook Configuration\n\nTo configure a webhook, open the dashboard and provide the endpoint URL."
                        ),
                        0.95
                )
        );

        when(technicalDocumentationService.findRelevantChunks(anyString(), eq(3)))
                .thenReturn(searchResults);

        when(llmClient.getResponseFromGemini(anyString())).thenReturn("""
                {
                  "status": "SUCCESS",
                  "message_to_user": "According to the integration guide, configure a webhook in the dashboard by providing the endpoint URL."
                }
                """);

        AgentExecutionResult result = technicalSpecialistAgent.execute(step, history);

        assertEquals(AgentExecutionStatus.SUCCESS, result.status());
        assertTrue(result.message().contains("integration guide"));

        verify(technicalDocumentationService).findRelevantChunks(
                "How do I configure webhooks?\nTask: Explain webhook configuration",
                3
        );
    }

    @Test
    void shouldReturnNeedsUserInputWhenQuestionIsTooVague() throws Exception {
        PlanStep step = buildTechnicalStep(
                "Troubleshoot integration",
                "My integration does not work",
                ""
        );

        when(technicalDocumentationService.findRelevantChunks(anyString(), eq(3)))
                .thenReturn(List.of());

        when(llmClient.getResponseFromGemini(anyString())).thenReturn("""
                {
                  "status": "NEEDS_USER_INPUT",
                  "message_to_user": "Please tell me whether the issue is related to authentication, webhooks, deployment, or another integration area."
                }
                """);

        AgentExecutionResult result = technicalSpecialistAgent.execute(step, new ConversationHistory());

        assertEquals(AgentExecutionStatus.NEEDS_USER_INPUT, result.status());
        assertTrue(result.message().contains("authentication"));
    }

    @Test
    void shouldReturnCannotHandleForOutOfScopeRequest() throws Exception {
        PlanStep step = buildTechnicalStep(
                "Answer favorite language question",
                "What is your favorite programming language?",
                ""
        );

        when(technicalDocumentationService.findRelevantChunks(anyString(), eq(3)))
                .thenReturn(List.of());

        when(llmClient.getResponseFromGemini(anyString())).thenReturn("""
                {
                  "status": "CANNOT_HANDLE",
                  "message_to_user": "This request is outside my technical documentation support scope."
                }
                """);

        AgentExecutionResult result = technicalSpecialistAgent.execute(step, new ConversationHistory());

        assertEquals(AgentExecutionStatus.CANNOT_HANDLE, result.status());
        assertTrue(result.message().contains("outside my technical documentation support scope"));
    }

    @Test
    void shouldThrowAbortWhenLlmReturnsAbort() throws Exception {
        PlanStep step = buildTechnicalStep(
                "Stop technical analysis",
                "Never mind, stop this",
                ""
        );

        when(technicalDocumentationService.findRelevantChunks(anyString(), eq(3)))
                .thenReturn(List.of());

        when(llmClient.getResponseFromGemini(anyString())).thenReturn("""
                {
                  "status": "ABORT",
                  "message_to_user": "Okay, I will stop working on this request."
                }
                """);

        PromptExecutionAbortException exception = assertThrows(
                PromptExecutionAbortException.class,
                () -> technicalSpecialistAgent.execute(step, new ConversationHistory())
        );

        assertEquals("Okay, I will stop working on this request.", exception.getMessage());
    }

    @Test
    void shouldThrowWhenStepDoesNotContainUsableQuery() {
        PlanStep step = new PlanStep();
        step.setAgent("TECHNICAL_SPECIALIST");
        step.setTask("   ");
        step.setOriginalMessageSection("   ");
        step.setAdditionalContext("   ");
        step.setConfidence(0.7);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> technicalSpecialistAgent.execute(step, new ConversationHistory())
        );

        assertEquals("Technical plan step does not contain a usable query", exception.getMessage());
        verifyNoInteractions(technicalDocumentationService, llmClient);
    }

    private PlanStep buildTechnicalStep(String task, String originalMessageSection, String additionalContext) {
        PlanStep step = new PlanStep();
        step.setAgent("TECHNICAL_SPECIALIST");
        step.setTask(task);
        step.setOriginalMessageSection(originalMessageSection);
        step.setAdditionalContext(additionalContext);
        step.setConfidence(0.93);
        return step;
    }
}