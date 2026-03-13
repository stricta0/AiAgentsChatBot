package org.example.agent.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.agent.AgentExecutionResult;
import org.example.agent.AgentExecutionStatus;
import org.example.agent.PromptExecutionAbortException;
import org.example.agent.SupportAgent;
import org.example.agent.prompt.BillingSpecialistErrorPromptDefinition;
import org.example.agent.prompt.BillingSpecialistErrorPromptFactory;
import org.example.agent.prompt.BillingSpecialistPromptDefinition;
import org.example.agent.prompt.BillingSpecialistPromptFactory;
import org.example.billing.BillingService;
import org.example.conversation.ConversationHistory;
import org.example.llm.LlmClient;
import org.example.router.model.PlanStep;

public class BillingSpecialistAgent implements SupportAgent {

    private final LlmClient llmClient;
    private final BillingService billingService;
    private final ObjectMapper objectMapper;
    private final BillingSpecialistPromptDefinition promptDefinition;
    private final BillingSpecialistPromptFactory promptFactory;
    private final BillingSpecialistErrorPromptDefinition errorPromptDefinition;
    private final BillingSpecialistErrorPromptFactory errorPromptFactory;

    public BillingSpecialistAgent(LlmClient llmClient, BillingService billingService) {
        this.llmClient = llmClient;
        this.billingService = billingService;
        this.objectMapper = new ObjectMapper();
        this.promptDefinition = BillingSpecialistPromptDefinition.load();
        this.promptFactory = new BillingSpecialistPromptFactory();
        this.errorPromptDefinition = BillingSpecialistErrorPromptDefinition.load();
        this.errorPromptFactory = new BillingSpecialistErrorPromptFactory();
    }

    @Override
    public String getName() {
        return "BILLING_SPECIALIST";
    }

    @Override
    public AgentExecutionResult execute(PlanStep step, ConversationHistory history) throws Exception {
        String prompt = promptFactory.buildPrompt(step, history, promptDefinition);
        String responseText = llmClient.getResponseFromGemini(prompt);
        String cleanJson = stripCodeFences(responseText);

        BillingDecision decision = objectMapper.readValue(cleanJson, BillingDecision.class);

        String status = safe(decision.status).toUpperCase();
        return switch (status) {
            case "PROCEED" -> executeOperation(step, history, decision);
            case "NEEDS_USER_INPUT" -> new AgentExecutionResult(
                    AgentExecutionStatus.NEEDS_USER_INPUT,
                    requireMessage(decision.messageToUser, "Billing agent returned NEEDS_USER_INPUT without message")
            );
            case "CANNOT_HANDLE" -> new AgentExecutionResult(
                    AgentExecutionStatus.CANNOT_HANDLE,
                    requireMessage(decision.messageToUser, "Billing agent returned CANNOT_HANDLE without message")
            );
            case "ABORT" -> throw new PromptExecutionAbortException(
                    requireMessage(decision.messageToUser, "Billing agent returned ABORT without message")
            );
            default -> throw new IllegalStateException("Unknown billing agent status: " + decision.status);
        };
    }

    private AgentExecutionResult executeOperation(
            PlanStep step,
            ConversationHistory history,
            BillingDecision decision
    ) throws Exception {
        String operation = safe(decision.operation).toUpperCase();

        try {
            return switch (operation) {
                case "CHECK_PLAN" -> handleCheckPlan(decision);
                case "CHECK_REFUND_POLICY" -> handleCheckRefundPolicy(decision);
                case "OPEN_REFUND_CASE" -> handleOpenRefundCase(decision);
                default -> throw new IllegalStateException("Unknown billing operation: " + decision.operation);
            };
        } catch (IllegalArgumentException | IllegalStateException e) {
            return handleExecutionFailure(step, history, decision, e);
        }
    }

    private AgentExecutionResult handleExecutionFailure(
            PlanStep step,
            ConversationHistory history,
            BillingDecision decision,
            Exception executionException
    ) throws Exception {
        String prompt = errorPromptFactory.buildPrompt(
                step,
                history,
                decision.operation,
                decision.email,
                decision.refundDescription,
                executionException.getMessage(),
                errorPromptDefinition
        );

        String responseText = llmClient.getResponseFromGemini(prompt);
        String cleanJson = stripCodeFences(responseText);

        BillingFailureDecision failureDecision =
                objectMapper.readValue(cleanJson, BillingFailureDecision.class);

        String status = safe(failureDecision.status).toUpperCase();

        return switch (status) {
            case "NEEDS_USER_INPUT" -> new AgentExecutionResult(
                    AgentExecutionStatus.NEEDS_USER_INPUT,
                    requireMessage(
                            failureDecision.messageToUser,
                            "Billing error handler returned NEEDS_USER_INPUT without message"
                    )
            );
            case "CANNOT_HANDLE" -> new AgentExecutionResult(
                    AgentExecutionStatus.CANNOT_HANDLE,
                    requireMessage(
                            failureDecision.messageToUser,
                            "Billing error handler returned CANNOT_HANDLE without message"
                    )
            );
            default -> throw new IllegalStateException(
                    "Unknown billing error handler status: " + failureDecision.status
            );
        };
    }

    private AgentExecutionResult handleCheckPlan(BillingDecision decision) {
        String email = requireValue(decision.email, "Missing email for CHECK_PLAN");

        BillingService.CustomerPlanDetails details = billingService.getCustomerPlanDetails(email);

        String message = """
                I found your current subscription details:
                - Customer: %s
                - Email: %s
                - Plan: %s
                - Monthly price: %s
                - Subscription status: %s
                - Started at: %s
                """.formatted(
                details.fullName(),
                details.email(),
                details.planName(),
                details.monthlyPrice(),
                details.subscriptionStatus(),
                details.startedAt()
        );

        return new AgentExecutionResult(AgentExecutionStatus.SUCCESS, message.trim());
    }

    private AgentExecutionResult handleCheckRefundPolicy(BillingDecision decision) {
        String email = requireValue(decision.email, "Missing email for CHECK_REFUND_POLICY");

        BillingService.CustomerRefundPolicyDetails details =
                billingService.getRefundPolicyDetails(email);

        String message = """
                I found the refund policy for your current subscription:
                - Customer: %s
                - Email: %s
                - Plan: %s
                - Refund available: %s
                - Refund window (days): %s
                - Processing time (days): %s
                - Policy description: %s
                """.formatted(
                details.fullName(),
                details.email(),
                details.planName(),
                details.refundAvailable(),
                details.refundWindowDays(),
                details.processingTimeDays(),
                details.policyDescription()
        );

        return new AgentExecutionResult(AgentExecutionStatus.SUCCESS, message.trim());
    }

    private AgentExecutionResult handleOpenRefundCase(BillingDecision decision) {
        String email = requireValue(decision.email, "Missing email for OPEN_REFUND_CASE");
        String description = requireValue(
                decision.refundDescription,
                "Missing refund description for OPEN_REFUND_CASE"
        );

        BillingService.RefundSupportCaseResult result =
                billingService.openRefundSupportCase(email, description);

        String message = """
                Your refund support case has been created successfully:
                - Case number: %s
                - Case type: %s
                - Status: %s
                - Description: %s
                - Created at: %s
                """.formatted(
                result.caseNumber(),
                result.caseType(),
                result.status(),
                result.description(),
                result.createdAt()
        );

        return new AgentExecutionResult(AgentExecutionStatus.SUCCESS, message.trim());
    }

    private String requireMessage(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(errorMessage);
        }

        return value.trim();
    }

    private String requireValue(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(errorMessage);
        }

        return value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String stripCodeFences(String text) {
        String trimmed = text.trim();

        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7).trim();
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3).trim();
        }

        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
        }

        return trimmed;
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private static class BillingDecision {
        public String status;
        public String operation;
        public String email;
        public String refundDescription;
        public String messageToUser;

        @JsonProperty("refund_description")
        public void setRefundDescription(String refundDescription) {
            this.refundDescription = refundDescription;
        }

        @JsonProperty("message_to_user")
        public void setMessageToUser(String messageToUser) {
            this.messageToUser = messageToUser;
        }
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private static class BillingFailureDecision {
        public String status;
        public String messageToUser;

        @JsonProperty("message_to_user")
        public void setMessageToUser(String messageToUser) {
            this.messageToUser = messageToUser;
        }
    }
}