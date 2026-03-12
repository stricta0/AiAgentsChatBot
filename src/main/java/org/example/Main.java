package org.example;

import org.example.conversation.ConversationHistory;
import org.example.llm.LlmClient;
import org.example.router.RouterService;
import org.example.router.UnknownResolutionService;
import org.example.ui.ConsoleChatApplication;
import org.example.ui.ConsoleCommands;
import org.example.ui.ConsoleMessages;
import org.example.ui.command.*;
import org.example.ui.command.context.ConsoleCommandContext;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        ConsoleMessages messages = ConsoleMessages.load();
        ConsoleCommands commands = ConsoleCommands.load();
        ConversationHistory history = new ConversationHistory();

        ConsoleCommandRegistry commandRegistry = new ConsoleCommandRegistry(
                List.of(
                        new HelpCommand(),
                        new ExitCommand(),
                        new AgentsCommand(),
                        new HistoryCommand()
                )
        );

        ConsoleCommandContext commandContext = new ConsoleCommandContext(
                messages,
                commands,
                history
        );



        try (Scanner scanner = new Scanner(System.in)) {
            LlmClient llmClient = new LlmClient();
            RouterService routerService = new RouterService(llmClient);

            UnknownResolutionService unknownResolutionService =
                    new UnknownResolutionService(llmClient, routerService);


            ConsoleChatApplication application = new ConsoleChatApplication(
                    routerService,
                    messages,
                    commands,
                    commandRegistry,
                    commandContext,
                    scanner,
                    unknownResolutionService,
                    history
            );

            application.run();
        }
    }
}