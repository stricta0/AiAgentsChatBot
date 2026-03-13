package org.example.agent;

import org.example.conversation.ConversationHistory;
import org.example.router.model.PlanStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlanStepExecutorTest {

    private SupportAgent billingAgent;
    private SupportAgent technicalAgent;
    private SupportAgent generalAgent;
    private AgentCatalog agentCatalog;
    private AgentRegistry agentRegistry;
    private PlanStepExecutor planStepExecutor;

    @BeforeEach
    void setUp() {
        billingAgent = mock(SupportAgent.class);
        technicalAgent = mock(SupportAgent.class);
        generalAgent = mock(SupportAgent.class);
        agentCatalog = mock(AgentCatalog.class);

        when(billingAgent.getName()).thenReturn("BILLING_SPECIALIST");
        when(technicalAgent.getName()).thenReturn("TECHNICAL_SPECIALIST");
        when(generalAgent.getName()).thenReturn("GENERAL");

        when(agentCatalog.getAgents()).thenReturn(List.of(
                new AgentDefinition(
                        "BILLING_SPECIALIST",
                        "Billing agent",
                        List.of(),
                        List.of(),
                        List.of()
                ),
                new AgentDefinition(
                        "TECHNICAL_SPECIALIST",
                        "Technical agent",
                        List.of(),
                        List.of(),
                        List.of()
                ),
                new AgentDefinition(
                        "GENERAL",
                        "General agent",
                        List.of(),
                        List.of(),
                        List.of()
                )
        ));

        agentRegistry = new AgentRegistry(
                List.of(billingAgent, technicalAgent, generalAgent),
                agentCatalog
        );

        planStepExecutor = new PlanStepExecutor(agentRegistry);
    }

    @Test
    void shouldExecuteStepUsingMatchingAgent() throws Exception {
        PlanStep step = new PlanStep();
        step.setAgent("BILLING_SPECIALIST");
        step.setTask("Check customer plan");

        ConversationHistory history = new ConversationHistory();

        AgentExecutionResult expectedResult =
                new AgentExecutionResult(AgentExecutionStatus.SUCCESS, "Done");

        when(billingAgent.execute(step, history)).thenReturn(expectedResult);

        AgentExecutionResult result = planStepExecutor.executeStep(step, history);

        assertEquals(AgentExecutionStatus.SUCCESS, result.status());
        assertEquals("Done", result.message());

        verify(billingAgent).execute(step, history);
        verify(technicalAgent, never()).execute(any(), any());
        verify(generalAgent, never()).execute(any(), any());
    }

    @Test
    void shouldThrowWhenStepIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planStepExecutor.executeStep(null, new ConversationHistory())
        );

        assertEquals("Plan step cannot be null", exception.getMessage());
    }

    @Test
    void shouldThrowWhenStepAgentIsBlank() {
        PlanStep step = new PlanStep();
        step.setAgent("   ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> planStepExecutor.executeStep(step, new ConversationHistory())
        );

        assertEquals("Plan step agent cannot be null or blank", exception.getMessage());
    }

    @Test
    void shouldThrowWhenNoAgentExistsForStep() {
        PlanStep step = new PlanStep();
        step.setAgent("UNKNOWN_AGENT");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> planStepExecutor.executeStep(step, new ConversationHistory())
        );

        assertEquals("No runtime agent registered for: UNKNOWN_AGENT", exception.getMessage());
    }
}