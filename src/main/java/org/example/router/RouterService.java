package org.example.router;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.conversation.ConversationHistory;
import org.example.llm.LlmClient;
import org.example.router.model.AgentCatalog;
import org.example.router.model.RouterPromptDefinition;
import org.example.router.model.RoutingPlan;

public class RouterService {

    private final LlmClient llmClient;
    private final RouterPromptFactory promptFactory;
    private final ObjectMapper objectMapper;
    private final AgentCatalog agentCatalog;
    private final RouterPromptDefinition routerPromptDefinition;

    public RouterService(LlmClient llmClient) {
        this.llmClient = llmClient;
        this.promptFactory = new RouterPromptFactory();
        this.objectMapper = new ObjectMapper();
        this.agentCatalog = AgentCatalog.load();
        this.routerPromptDefinition = RouterPromptDefinition.load();
    }

    public RoutingPlan route(String userMessage, ConversationHistory history) throws Exception {
        String prompt = promptFactory.buildPrompt(
                userMessage,
                agentCatalog.getAgents(),
                routerPromptDefinition
        );

        String responseText = llmClient.getResponseFromGemini(prompt, history);
        String cleanJson = stripCodeFences(responseText);
        return objectMapper.readValue(cleanJson, RoutingPlan.class);
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