package org.example.ui.command;

import org.example.router.model.AgentCatalog;
import org.example.router.model.AgentDefinition;
import org.example.ui.command.context.ConsoleCommandContext;

import java.util.List;

public class AgentsCommand implements ConsoleCommandHandler {

    @Override
    public String getName() {
        return "agents";
    }

    @Override
    public String execute(ConsoleCommandContext context) {
        AgentCatalog agentCatalog = AgentCatalog.load();
        List<AgentDefinition> agents = agentCatalog.getAgents();

        StringBuilder sb = new StringBuilder();
        sb.append("=== AVAILABLE AGENTS ===\n");

        if (agents == null || agents.isEmpty()) {
            sb.append("No agents found.");
            return sb.toString();
        }

        for (AgentDefinition agent : agents) {
            sb.append("- ").append(agent.getName()).append("\n");
            sb.append("  Description: ").append(agent.getDescription()).append("\n");

            appendList(sb, "  Can handle:", agent.getCanHandle());
            appendList(sb, "  Cannot handle:", agent.getCannotHandle());
            appendList(sb, "  Examples:", agent.getExamples());

            sb.append("\n");
        }

        return sb.toString().trim();
    }

    private void appendList(StringBuilder sb, String header, List<String> items) {
        sb.append(header).append("\n");

        if (items == null || items.isEmpty()) {
            sb.append("    - none\n");
            return;
        }

        for (String item : items) {
            sb.append("    - ").append(item).append("\n");
        }
    }
}