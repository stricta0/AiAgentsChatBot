package org.example.config;

public final class ResourcePaths {

    private ResourcePaths() {
    }

    public static final String TECHNICAL_SPECIALIST_PROMPT =
            "agent/technical_specialist_prompt.json";

    public static final String BILLING_SPECIALIST_PROMPT =
            "agent/billing_specialist_prompt.json";

    public static final String BILLING_SPECIALIST_ERROR_PROMPT =
            "agent/billing_specialist_error_prompt.json";

    public static final String ROUTER_PROMPT =
            "router/router_prompt.json";

    public static final String UNKNOWN_RESOLUTION_PROMPT =
            "router/unknown_resolution_prompt.json";

    public static final String AGENTS_CATALOG =
            "router/agents.json";

    public static final String CONSOLE_MESSAGES =
            "ui/console_messages.json";

    public static final String CONSOLE_COMMANDS =
            "ui/console_commands.json";

    public static final String TECHNICAL_DOCS_CONFIG =
            "docs/docs_config.json";

    public static final String DB_INIT_SQL =
            "db/init.sql";
}