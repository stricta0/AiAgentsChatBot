package org.example.agent.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.JsonResourceLoader;
import org.example.config.ResourcePaths;
import org.example.conversation.ConversationHistory;
import org.example.router.model.PlanStep;
import org.example.technicaldocs.model.TechnicalDocumentChunk;
import org.example.technicaldocs.model.TechnicalSearchResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TechnicalSpecialistPromptFactoryTest {

    @Test
    void shouldBuildPromptContainingStepHistoryAndRetrievedDocs() {
        TechnicalSpecialistPromptFactory factory = new TechnicalSpecialistPromptFactory();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonResourceLoader jsonResourceLoader = new JsonResourceLoader(objectMapper);

        TechnicalSpecialistPromptDefinition definition =
                jsonResourceLoader.load(
                        ResourcePaths.TECHNICAL_SPECIALIST_PROMPT,
                        TechnicalSpecialistPromptDefinition.class
                );

        PlanStep step = new PlanStep();
        step.setAgent("TECHNICAL_SPECIALIST");
        step.setTask("Explain webhook configuration");
        step.setOriginalMessageSection("How do I configure webhooks?");
        step.setAdditionalContext("User is asking about integration setup");
        step.setConfidence(0.92);

        ConversationHistory history = new ConversationHistory();
        history.addUserMessage("How do I configure webhooks?");

        TechnicalSearchResult result = new TechnicalSearchResult(
                new TechnicalDocumentChunk(
                        "integration.md",
                        ".md",
                        "Integration Guide > Webhook Configuration",
                        2,
                        "Heading Path: Integration Guide > Webhook Configuration\n\nConfigure a webhook in the dashboard."
                ),
                0.91
        );

        String prompt = factory.buildPrompt(step, history, List.of(result), definition);

        assertTrue(prompt.contains("TECHNICAL_SPECIALIST"));
        assertTrue(prompt.contains("How do I configure webhooks?"));
        assertTrue(prompt.contains("integration.md"));
        assertTrue(prompt.contains("Webhook Configuration"));
        assertTrue(prompt.contains("similarity_score"));
        assertTrue(prompt.contains("Configure a webhook in the dashboard."));
    }

    @Test
    void shouldBuildPromptWithNoDocumentationMessageWhenResultsAreEmpty() {
        TechnicalSpecialistPromptFactory factory = new TechnicalSpecialistPromptFactory();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonResourceLoader jsonResourceLoader = new JsonResourceLoader(objectMapper);

        TechnicalSpecialistPromptDefinition definition =
                jsonResourceLoader.load(
                        ResourcePaths.TECHNICAL_SPECIALIST_PROMPT,
                        TechnicalSpecialistPromptDefinition.class
                );

        PlanStep step = new PlanStep();
        step.setAgent("TECHNICAL_SPECIALIST");
        step.setTask("Explain authentication");
        step.setOriginalMessageSection("How do I authenticate API requests?");
        step.setAdditionalContext("");
        step.setConfidence(0.95);

        String prompt = factory.buildPrompt(step, new ConversationHistory(), List.of(), definition);

        assertTrue(prompt.contains("No documentation excerpts were retrieved."));
    }
}