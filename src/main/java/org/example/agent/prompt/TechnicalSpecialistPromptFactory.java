package org.example.agent.prompt;

import org.example.conversation.ConversationHistory;
import org.example.router.model.PlanStep;
import org.example.technicaldocs.model.TechnicalSearchResult;

import java.util.List;
import java.util.stream.Collectors;

public class TechnicalSpecialistPromptFactory {

    public String buildPrompt(
            PlanStep step,
            ConversationHistory history,
            List<TechnicalSearchResult> searchResults,
            TechnicalSpecialistPromptDefinition promptDefinition
    ) {
        return """
                TASK
                %s

                RULES
                %s

                OUTPUT FORMAT
                %s

                CURRENT STEP
                %s

                CONVERSATION HISTORY
                %s

                RETRIEVED DOCUMENTATION EXCERPTS
                %s
                """.formatted(
                renderTask(promptDefinition.getTaskLines()),
                renderRules(promptDefinition.getRules()),
                promptDefinition.getOutputFormat(),
                renderStep(step),
                quoteBlock(history != null ? history.toPromptString() : ""),
                renderSearchResults(searchResults)
        );
    }

    private String renderTask(List<String> taskLines) {
        return String.join("\n", taskLines);
    }

    private String renderRules(List<String> rules) {
        return rules.stream()
                .map(rule -> "- " + rule)
                .collect(Collectors.joining("\n"));
    }

    private String renderStep(PlanStep step) {
        return quoteBlock("""
                agent: %s
                task: %s
                original_message_section: %s
                additional_context: %s
                confidence: %s
                """.formatted(
                safe(step.getAgent()),
                safe(step.getTask()),
                safe(step.getOriginalMessageSection()),
                safe(step.getAdditionalContext()),
                step.getConfidence()
        ));
    }

    private String renderSearchResults(List<TechnicalSearchResult> searchResults) {
        if (searchResults == null || searchResults.isEmpty()) {
            return quoteBlock("No documentation excerpts were retrieved.");
        }

        String rendered = searchResults.stream()
                .map(this::renderSingleResult)
                .collect(Collectors.joining("\n\n"));

        return quoteBlock(rendered);
    }

    private String renderSingleResult(TechnicalSearchResult result) {
        return """
                document_name: %s
                document_type: %s
                heading_path: %s
                chunk_index: %s
                similarity_score: %s
                content:
                %s
                """.formatted(
                safe(result.chunk().documentName()),
                safe(result.chunk().documentType()),
                safe(result.chunk().headingPath()),
                result.chunk().chunkIndex(),
                result.similarityScore(),
                safe(result.chunk().content())
        );
    }

    private String quoteBlock(String text) {
        return "\"\"\"\n" + text + "\n\"\"\"";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}