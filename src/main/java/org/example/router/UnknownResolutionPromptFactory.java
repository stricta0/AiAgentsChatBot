package org.example.router;

import org.example.agent.AgentDefinition;
import org.example.router.model.RouterPromptDefinition;
import org.example.router.model.RoutingPlan;
import org.example.router.model.UnknownResolutionPromptDefinition;

import java.util.List;
import java.util.stream.Collectors;

public class UnknownResolutionPromptFactory {

    private final RouterPromptFactory routerPromptFactory;

    public UnknownResolutionPromptFactory() {
        this.routerPromptFactory = new RouterPromptFactory();
    }

    public String buildPrompt(
            String originalUserMessage,
            String userResolutionMessage,
            RoutingPlan previousRoutingPlan,
            List<AgentDefinition> agents,
            RouterPromptDefinition routerPromptDefinition,
            UnknownResolutionPromptDefinition unknownResolutionPromptDefinition
    ) {
        String previousRouterPrompt = routerPromptFactory.buildPrompt(
                originalUserMessage,
                agents,
                routerPromptDefinition
        );

        return """
                TASK
                %s

                RULES
                %s

                OUTPUT FORMAT
                %s

                PREVIOUS ROUTER PROMPT
                %s

                PREVIOUS ROUTING PLAN
                %s

                USER CLARIFICATION
                %s
                """.formatted(
                renderTask(unknownResolutionPromptDefinition.getTaskLines()),
                renderRules(unknownResolutionPromptDefinition.getRules()),
                unknownResolutionPromptDefinition.getOutputFormat(),
                quoteBlock(previousRouterPrompt),
                quoteBlock(previousRoutingPlan.toPrettyString()),
                quoteBlock(userResolutionMessage)
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

    private String quoteBlock(String text) {
        return "\"\"\"\n" + text + "\n\"\"\"";
    }
}