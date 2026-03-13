package org.example.agent.impl;

import org.example.agent.AgentExecutionResult;
import org.example.agent.AgentExecutionStatus;
import org.example.agent.PromptExecutionAbortException;
import org.example.billing.BillingService;
import org.example.conversation.ConversationHistory;
import org.example.llm.LlmClient;
import org.example.router.model.PlanStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BillingSpecialistAgentTest {

    private LlmClient llmClient;
    private BillingService billingService;
    private BillingSpecialistAgent billingSpecialistAgent;

    @BeforeEach
    void setUp() {
        llmClient = mock(LlmClient.class);
        billingService = mock(BillingService.class);

        billingSpecialistAgent = new BillingSpecialistAgent(llmClient, billingService);
    }

    @Test
    void shouldReturnSuccessForCheckPlan() throws Exception {
        PlanStep step = buildBillingStep("Check customer's current plan");
        ConversationHistory history = new ConversationHistory();

        when(llmClient.getResponseFromGemini(anyString())).thenReturn("""
                {
                  "status": "PROCEED",
                  "operation": "CHECK_PLAN",
                  "email": "john.doe@example.com",
                  "refund_description": "",
                  "message_to_user": ""
                }
                """);

        when(billingService.getCustomerPlanDetails("john.doe@example.com"))
                .thenReturn(new BillingService.CustomerPlanDetails(
                        1,
                        "john.doe@example.com",
                        "John Doe",
                        "PRO",
                        new BigDecimal("29.99"),
                        "ACTIVE",
                        "2026-03-01T10:00"
                ));

        AgentExecutionResult result = billingSpecialistAgent.execute(step, history);

        assertEquals(AgentExecutionStatus.SUCCESS, result.status());
        assertTrue(result.message().contains("John Doe"));
        assertTrue(result.message().contains("john.doe@example.com"));
        assertTrue(result.message().contains("PRO"));
        assertTrue(result.message().contains("29.99"));
        assertTrue(result.message().contains("ACTIVE"));

        verify(billingService).getCustomerPlanDetails("john.doe@example.com");
    }

    @Test
    void shouldReturnSuccessForRefundPolicy() throws Exception {
        PlanStep step = buildBillingStep("Explain refund policy");
        ConversationHistory history = new ConversationHistory();

        when(llmClient.getResponseFromGemini(anyString())).thenReturn("""
                {
                  "status": "PROCEED",
                  "operation": "CHECK_REFUND_POLICY",
                  "email": "john.doe@example.com",
                  "refund_description": "",
                  "message_to_user": ""
                }
                """);

        when(billingService.getRefundPolicyDetails("john.doe@example.com"))
                .thenReturn(new BillingService.CustomerRefundPolicyDetails(
                        1,
                        "john.doe@example.com",
                        "John Doe",
                        "PRO",
                        true,
                        14,
                        3,
                        "Pro plan can be refunded within 14 days of purchase."
                ));

        AgentExecutionResult result = billingSpecialistAgent.execute(step, history);

        assertEquals(AgentExecutionStatus.SUCCESS, result.status());
        assertTrue(result.message().contains("John Doe"));
        assertTrue(result.message().contains("PRO"));
        assertTrue(result.message().contains("true"));
        assertTrue(result.message().contains("14"));
        assertTrue(result.message().contains("3"));

        verify(billingService).getRefundPolicyDetails("john.doe@example.com");
    }

    @Test
    void shouldReturnSuccessForOpenRefundCase() throws Exception {
        PlanStep step = buildBillingStep("Open refund support case");
        ConversationHistory history = new ConversationHistory();

        when(llmClient.getResponseFromGemini(anyString())).thenReturn("""
                {
                  "status": "PROCEED",
                  "operation": "OPEN_REFUND_CASE",
                  "email": "john.doe@example.com",
                  "refund_description": "Customer wants a refund.",
                  "message_to_user": ""
                }
                """);

        when(billingService.openRefundSupportCase("john.doe@example.com", "Customer wants a refund."))
                .thenReturn(new BillingService.RefundSupportCaseResult(
                        100,
                        "REFUND-123456",
                        1,
                        "REFUND",
                        "OPEN",
                        "Customer wants a refund.",
                        "2026-03-13T11:30"
                ));

        AgentExecutionResult result = billingSpecialistAgent.execute(step, history);

        assertEquals(AgentExecutionStatus.SUCCESS, result.status());
        assertTrue(result.message().contains("REFUND-123456"));
        assertTrue(result.message().contains("REFUND"));
        assertTrue(result.message().contains("OPEN"));

        verify(billingService).openRefundSupportCase("john.doe@example.com", "Customer wants a refund.");
    }

    @Test
    void shouldReturnNeedsUserInputWhenLlmRequestsMoreData() throws Exception {
        PlanStep step = buildBillingStep("Check plan");
        ConversationHistory history = new ConversationHistory();

        when(llmClient.getResponseFromGemini(anyString())).thenReturn("""
                {
                  "status": "NEEDS_USER_INPUT",
                  "operation": "NONE",
                  "email": "",
                  "refund_description": "",
                  "message_to_user": "Please provide your email address."
                }
                """);

        AgentExecutionResult result = billingSpecialistAgent.execute(step, history);

        assertEquals(AgentExecutionStatus.NEEDS_USER_INPUT, result.status());
        assertEquals("Please provide your email address.", result.message());

        verifyNoInteractions(billingService);
    }

    @Test
    void shouldReturnCannotHandleWhenLlmSaysCannotHandle() throws Exception {
        PlanStep step = buildBillingStep("Do something unsupported");
        ConversationHistory history = new ConversationHistory();

        when(llmClient.getResponseFromGemini(anyString())).thenReturn("""
                {
                  "status": "CANNOT_HANDLE",
                  "operation": "NONE",
                  "email": "",
                  "refund_description": "",
                  "message_to_user": "I cannot handle this billing request."
                }
                """);

        AgentExecutionResult result = billingSpecialistAgent.execute(step, history);

        assertEquals(AgentExecutionStatus.CANNOT_HANDLE, result.status());
        assertEquals("I cannot handle this billing request.", result.message());

        verifyNoInteractions(billingService);
    }

    @Test
    void shouldThrowAbortExceptionWhenLlmReturnsAbort() throws Exception {
        PlanStep step = buildBillingStep("Check plan");
        ConversationHistory history = new ConversationHistory();

        when(llmClient.getResponseFromGemini(anyString())).thenReturn("""
                {
                  "status": "ABORT",
                  "operation": "NONE",
                  "email": "",
                  "refund_description": "",
                  "message_to_user": "Okay, I will stop working on this request."
                }
                """);

        PromptExecutionAbortException exception = assertThrows(
                PromptExecutionAbortException.class,
                () -> billingSpecialistAgent.execute(step, history)
        );

        assertEquals("Okay, I will stop working on this request.", exception.getMessage());
        verifyNoInteractions(billingService);
    }

    @Test
    void shouldReturnCannotHandleWhenBillingServiceFailsDuringCheckPlan() throws Exception {
        PlanStep step = buildBillingStep("Check plan");
        ConversationHistory history = new ConversationHistory();

        when(llmClient.getResponseFromGemini(anyString()))
                .thenReturn("""
                        {
                          "status": "PROCEED",
                          "operation": "CHECK_PLAN",
                          "email": "missing@example.com",
                          "refund_description": "",
                          "message_to_user": ""
                        }
                        """)
                .thenReturn("""
                        {
                          "status": "CANNOT_HANDLE",
                          "message_to_user": "I cannot complete this billing request."
                        }
                        """);

        when(billingService.getCustomerPlanDetails("missing@example.com"))
                .thenThrow(new IllegalStateException("Customer not found for email: missing@example.com"));

        AgentExecutionResult result = billingSpecialistAgent.execute(step, history);

        assertEquals(AgentExecutionStatus.CANNOT_HANDLE, result.status());
        assertEquals("I cannot complete this billing request.", result.message());

        verify(billingService).getCustomerPlanDetails("missing@example.com");
    }

    @Test
    void shouldReturnCannotHandleWhenOperationIsUnknown() throws Exception {
        PlanStep step = buildBillingStep("Unknown billing task");
        ConversationHistory history = new ConversationHistory();

        when(llmClient.getResponseFromGemini(anyString()))
                .thenReturn("""
                        {
                          "status": "PROCEED",
                          "operation": "SOMETHING_ELSE",
                          "email": "john.doe@example.com",
                          "refund_description": "",
                          "message_to_user": ""
                        }
                        """)
                .thenReturn("""
                        {
                          "status": "CANNOT_HANDLE",
                          "message_to_user": "I cannot handle this billing request."
                        }
                        """);

        AgentExecutionResult result = billingSpecialistAgent.execute(step, history);

        assertEquals(AgentExecutionStatus.CANNOT_HANDLE, result.status());
        assertEquals("I cannot handle this billing request.", result.message());

        verifyNoInteractions(billingService);
    }

    private PlanStep buildBillingStep(String task) {
        PlanStep step = new PlanStep();
        step.setAgent("BILLING_SPECIALIST");
        step.setTask(task);
        step.setOriginalMessageSection(task);
        step.setAdditionalContext("");
        step.setConfidence(0.95);
        return step;
    }
}