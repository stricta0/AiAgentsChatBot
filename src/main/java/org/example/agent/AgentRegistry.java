package org.example.agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentRegistry {

    private final Map<String, SupportAgent> agentsByName;
    private final AgentCatalog agentCatalog;

    public AgentRegistry(List<SupportAgent> agents) {
        this.agentsByName = new HashMap<>();
        this.agentCatalog = AgentCatalog.load();

        for (SupportAgent agent : agents) {
            String agentName = normalize(agent.getName());

            if (agentName.isBlank()) {
                throw new IllegalStateException("Support agent name cannot be null or blank");
            }

            if (agentsByName.containsKey(agentName)) {
                throw new IllegalStateException(
                        "Duplicate runtime agent implementation for agent: " + agent.getName()
                );
            }

            agentsByName.put(agentName, agent);
        }

        validateConfiguredAgents();
        validateRegisteredAgents();
    }

    public SupportAgent getAgent(String agentName) {
        String normalizedAgentName = normalize(agentName);

        if (normalizedAgentName.isBlank()) {
            throw new IllegalArgumentException("Agent name cannot be null or blank");
        }

        SupportAgent agent = agentsByName.get(normalizedAgentName);

        if (agent == null) {
            throw new IllegalStateException("No runtime agent registered for: " + agentName);
        }

        return agent;
    }

    private void validateConfiguredAgents() {
        for (AgentDefinition definition : agentCatalog.getAgents()) {
            String configuredAgentName = normalize(definition.getName());

            if (configuredAgentName.isBlank()) {
                throw new IllegalStateException("Agent name in agents.json cannot be null or blank");
            }

            if (!agentsByName.containsKey(configuredAgentName)) {
                throw new IllegalStateException(
                        "Missing runtime implementation for agent defined in agents.json: " + definition.getName()
                );
            }
        }
    }

    private void validateRegisteredAgents() {
        Map<String, Boolean> configuredAgentNames = new HashMap<>();

        for (AgentDefinition definition : agentCatalog.getAgents()) {
            configuredAgentNames.put(normalize(definition.getName()), true);
        }

        for (String registeredAgentName : agentsByName.keySet()) {
            if (!configuredAgentNames.containsKey(registeredAgentName)) {
                throw new IllegalStateException(
                        "Missing agent definition in agents.json for runtime agent: " + registeredAgentName
                );
            }
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}