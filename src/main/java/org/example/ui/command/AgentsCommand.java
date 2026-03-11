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
    public void execute(ConsoleCommandContext context) {
        AgentCatalog agentCatalog = AgentCatalog.load();
        List<AgentDefinition> agents = agentCatalog.getAgents();

        System.out.println();
        System.out.println("=== AVAILABLE AGENTS ===");

        if (agents == null || agents.isEmpty()) {
            System.out.println("No agents found.");
            System.out.println();
            return;
        }

        for (AgentDefinition agent : agents) {
            System.out.println("- " + agent.getName());
            System.out.println("  Description: " + agent.getDescription());

            printList("  Can handle:", agent.getCanHandle());
            printList("  Cannot handle:", agent.getCannotHandle());
            printList("  Examples:", agent.getExamples());

            System.out.println();
        }
    }

    private void printList(String header, List<String> items) {
        System.out.println(header);

        if (items == null || items.isEmpty()) {
            System.out.println("    - none");
            return;
        }

        for (String item : items) {
            System.out.println("    - " + item);
        }
    }
}