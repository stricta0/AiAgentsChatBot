package org.example;

import org.example.llm.LlmClient;
import org.example.router.RouterService;
import org.example.ui.ConsoleChatApplication;
import org.example.ui.ConsoleCommands;
import org.example.ui.ConsoleMessages;
import org.example.ui.command.ConsoleCommandRegistry;
import org.example.ui.command.ExitCommand;
import org.example.ui.command.HelpCommand;
import org.example.ui.command.AgentsCommand;
import org.example.ui.command.context.ConsoleCommandContext;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ConsoleMessages messages = ConsoleMessages.load();
        ConsoleCommands commands = ConsoleCommands.load();

        ConsoleCommandRegistry commandRegistry = new ConsoleCommandRegistry(
                List.of(
                        new HelpCommand(),
                        new ExitCommand(),
                        new AgentsCommand()
                )
        );

        ConsoleCommandContext commandContext = new ConsoleCommandContext(
                messages,
                commands
        );


        try (Scanner scanner = new Scanner(System.in)) {
            LlmClient llmClient = new LlmClient();
            RouterService routerService = new RouterService(llmClient);

            ConsoleChatApplication application = new ConsoleChatApplication(
                    routerService,
                    messages,
                    commands,
                    commandRegistry,
                    commandContext,
                    scanner
            );

            application.run();
        }
    }
}