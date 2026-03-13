package org.example.router;

import org.example.agent.AgentDefinition;
import org.example.router.model.RouterPromptDefinition;

import java.util.List;
import java.util.stream.Collectors;

public class RouterPromptFactory {

    public String buildPrompt(
            String userMessage,
            List<AgentDefinition> agents,
            RouterPromptDefinition promptDefinition
    ) {
        return """
                TASK
                %s

                AVAILABLE AGENTS
                %s

                RULES
                %s

                USER MESSAGE
                %s
                """.formatted(
                renderTask(promptDefinition.getTask()),
                renderAgents(agents),
                renderRules(promptDefinition.getRules()),
                quoteBlock(userMessage)
        );
    }

    private String renderAgents(List<AgentDefinition> agents) {
        return agents.stream()
                .map(this::renderAgent)
                .collect(Collectors.joining("\n\n"));
    }

    private String renderAgent(AgentDefinition agent) {
        return """
                - %s
                  Description: %s
                  Can handle:
                %s
                  Cannot handle:
                %s
                  Examples:
                %s
                """.formatted(
                agent.getName(),
                agent.getDescription(),
                renderList(agent.getCanHandle()),
                renderList(agent.getCannotHandle()),
                renderList(agent.getExamples())
        );
    }

    private String renderRules(List<String> rules) {
        return rules.stream()
                .map(rule -> "- " + rule)
                .collect(Collectors.joining("\n"));
    }

    private String renderTask(List<String> taskLines) {
        return String.join("\n", taskLines);
    }

    private String renderList(List<String> items) {
        return items.stream()
                .map(item -> "    - " + item)
                .collect(Collectors.joining("\n"));
    }

    private String quoteBlock(String text) {
        return "\"\"\"\n" + text + "\n\"\"\"";
    }
}