package org.example.agent.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.agent.AgentExecutionResult;
import org.example.agent.AgentExecutionStatus;
import org.example.agent.PromptExecutionAbortException;
import org.example.agent.SupportAgent;
import org.example.agent.prompt.TechnicalSpecialistPromptDefinition;
import org.example.agent.prompt.TechnicalSpecialistPromptFactory;
import org.example.conversation.ConversationHistory;
import org.example.llm.LlmClient;
import org.example.router.model.PlanStep;
import org.example.technicaldocs.TechnicalDocumentationService;
import org.example.technicaldocs.model.TechnicalSearchResult;

import java.util.List;

public class TechnicalSpecialistAgent implements SupportAgent {

    private final LlmClient llmClient;
    private final TechnicalDocumentationService technicalDocumentationService;
    private final ObjectMapper objectMapper;
    private final TechnicalSpecialistPromptDefinition promptDefinition;
    private final TechnicalSpecialistPromptFactory promptFactory;

    public TechnicalSpecialistAgent(
            LlmClient llmClient,
            TechnicalDocumentationService technicalDocumentationService
    ) {
        this.llmClient = llmClient;
        this.technicalDocumentationService = technicalDocumentationService;
        this.objectMapper = new ObjectMapper();
        this.promptDefinition = TechnicalSpecialistPromptDefinition.load();
        this.promptFactory = new TechnicalSpecialistPromptFactory();
    }

    @Override
    public String getName() {
        return "TECHNICAL_SPECIALIST";
    }

    @Override
    public AgentExecutionResult execute(PlanStep step, ConversationHistory history) throws Exception {
        String query = buildSearchQuery(step);

        List<TechnicalSearchResult> searchResults = technicalDocumentationService.findRelevantChunks(
                query,
                promptDefinition.getTopK()
        );

        String prompt = promptFactory.buildPrompt(
                step,
                history,
                searchResults,
                promptDefinition
        );

        String responseText = llmClient.getResponseFromGemini(prompt);
        String cleanJson = stripCodeFences(responseText);

        TechnicalDecision decision = objectMapper.readValue(cleanJson, TechnicalDecision.class);

        String status = safe(decision.status).toUpperCase();

        return switch (status) {
            case "SUCCESS" -> new AgentExecutionResult(
                    AgentExecutionStatus.SUCCESS,
                    requireMessage(decision.messageToUser, "Technical agent returned SUCCESS without message")
            );
            case "NEEDS_USER_INPUT" -> new AgentExecutionResult(
                    AgentExecutionStatus.NEEDS_USER_INPUT,
                    requireMessage(
                            decision.messageToUser,
                            "Technical agent returned NEEDS_USER_INPUT without message"
                    )
            );
            case "CANNOT_HANDLE" -> new AgentExecutionResult(
                    AgentExecutionStatus.CANNOT_HANDLE,
                    requireMessage(
                            decision.messageToUser,
                            "Technical agent returned CANNOT_HANDLE without message"
                    )
            );
            case "ABORT" -> throw new PromptExecutionAbortException(
                    requireMessage(decision.messageToUser, "Technical agent returned ABORT without message")
            );
            default -> throw new IllegalStateException("Unknown technical agent status: " + decision.status);
        };
    }

    private String buildSearchQuery(PlanStep step) {
        String task = safe(step.getTask()).trim();
        String originalMessageSection = safe(step.getOriginalMessageSection()).trim();
        String additionalContext = safe(step.getAdditionalContext()).trim();

        StringBuilder sb = new StringBuilder();

        if (!originalMessageSection.isBlank()) {
            sb.append(originalMessageSection);
        }

        if (!task.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append("Task: ").append(task);
        }

        if (!additionalContext.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append("\n");
            }
            sb.append("Additional context: ").append(additionalContext);
        }

        String query = sb.toString().trim();

        if (query.isBlank()) {
            throw new IllegalArgumentException("Technical plan step does not contain a usable query");
        }

        return query;
    }

    private String requireMessage(String value, String errorMessage) {
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
    private static class TechnicalDecision {
        public String status;
        public String messageToUser;

        @JsonProperty("message_to_user")
        public void setMessageToUser(String messageToUser) {
            this.messageToUser = messageToUser;
        }
    }
}