package org.example.router;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.conversation.ConversationHistory;
import org.example.llm.LlmClient;
import org.example.agent.AgentCatalog;
import org.example.router.model.RouterPromptDefinition;
import org.example.router.model.RoutingPlan;
import org.example.router.model.UnknownResolutionPromptDefinition;

import java.util.Optional;

public class UnknownResolutionService {

    private final LlmClient llmClient;
    private final RouterService routerService;
    private final ObjectMapper objectMapper;
    private final AgentCatalog agentCatalog;
    private final RouterPromptDefinition routerPromptDefinition;
    private final UnknownResolutionPromptDefinition unknownResolutionPromptDefinition;
    private final UnknownResolutionPromptFactory unknownResolutionPromptFactory;

    public UnknownResolutionService(LlmClient llmClient, RouterService routerService) {
        this.llmClient = llmClient;
        this.routerService = routerService;
        this.objectMapper = new ObjectMapper();
        this.agentCatalog = AgentCatalog.load();
        this.routerPromptDefinition = RouterPromptDefinition.load();
        this.unknownResolutionPromptDefinition = UnknownResolutionPromptDefinition.load();
        this.unknownResolutionPromptFactory = new UnknownResolutionPromptFactory();
    }

    public Optional<RoutingPlan> resolve(
            RoutingPlan routingPlan,
            String userResolutionMessage,
            ConversationHistory history
    ) throws Exception {

        if (!routingPlan.hasUnknownSteps()) {
            return Optional.of(routingPlan);
        }

        String prompt = unknownResolutionPromptFactory.buildPrompt(
                routingPlan.getWholeOriginalMessage(),
                userResolutionMessage,
                routingPlan,
                agentCatalog.getAgents(),
                routerPromptDefinition,
                unknownResolutionPromptDefinition
        );

        String responseText = llmClient.getResponseFromGemini(prompt, history);
        String cleanJson = stripCodeFences(responseText);

        JsonNode root = objectMapper.readTree(cleanJson);
        String status = root.path("status").asText();

        if ("ABORT".equalsIgnoreCase(status)) {
            return Optional.empty();
        }

        if (!"RESOLVED".equalsIgnoreCase(status)) {
            throw new IllegalStateException(
                    "Unknown resolution status: " + status + ". Raw response: " + cleanJson
            );
        }

        JsonNode routingPlanNode = root.path("routing_plan");
        if (routingPlanNode.isMissingNode() || routingPlanNode.isNull()) {
            throw new IllegalStateException(
                    "Missing routing_plan in unknown resolution response: " + cleanJson
            );
        }

        RoutingPlan resolvedRoutingPlan =
                objectMapper.treeToValue(routingPlanNode, RoutingPlan.class);

        return Optional.of(resolvedRoutingPlan);
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
}