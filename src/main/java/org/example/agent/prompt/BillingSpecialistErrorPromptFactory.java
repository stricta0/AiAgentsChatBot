package org.example.agent.prompt;

import org.example.conversation.ConversationHistory;
import org.example.router.model.PlanStep;

import java.util.List;
import java.util.stream.Collectors;

public class BillingSpecialistErrorPromptFactory {

    public String buildPrompt(
            PlanStep step,
            ConversationHistory history,
            String operation,
            String email,
            String refundDescription,
            String executionError,
            BillingSpecialistErrorPromptDefinition promptDefinition
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

                ATTEMPTED OPERATION
                %s

                INPUT DATA
                %s

                EXECUTION ERROR
                %s

                CONVERSATION HISTORY
                %s
                """.formatted(
                renderTask(promptDefinition.getTaskLines()),
                renderRules(promptDefinition.getRules()),
                promptDefinition.getOutputFormat(),
                renderStep(step),
                quoteBlock(safe(operation)),
                renderInputData(email, refundDescription),
                quoteBlock(safe(executionError)),
                quoteBlock(history != null ? history.toPromptString() : "")
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

    private String renderInputData(String email, String refundDescription) {
        return quoteBlock("""
                email: %s
                refund_description: %s
                """.formatted(
                safe(email),
                safe(refundDescription)
        ));
    }

    private String quoteBlock(String text) {
        return "\"\"\"\n" + text + "\n\"\"\"";
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}