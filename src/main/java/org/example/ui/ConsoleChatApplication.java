package org.example.ui;

import org.example.router.RouterService;
import org.example.router.model.RoutingPlan;
import org.example.ui.command.ConsoleCommandHandler;
import org.example.ui.command.ConsoleCommandRegistry;
import org.example.ui.command.context.ConsoleCommandContext;

import java.util.Scanner;

public class ConsoleChatApplication {

    private final RouterService routerService;
    private final ConsoleMessages messages;
    private final ConsoleCommands commands;
    private final ConsoleCommandRegistry commandRegistry;
    private final ConsoleCommandContext commandContext;
    private final Scanner scanner;

    public ConsoleChatApplication(
            RouterService routerService,
            ConsoleMessages messages,
            ConsoleCommands commands,
            ConsoleCommandRegistry commandRegistry,
            ConsoleCommandContext commandContext,
            Scanner scanner
    ) {
        this.routerService = routerService;
        this.messages = messages;
        this.commands = commands;
        this.commandRegistry = commandRegistry;
        this.commandContext = commandContext;
        this.scanner = scanner;
    }

    public void run() {

        printWelcome();

        while (!commandContext.isExitRequested()) {

            System.out.print(messages.getPrompt());
            String userInput = scanner.nextLine().trim();

            if (userInput.isBlank()) {
                System.out.println(messages.getEmptyMessageWarning());
                continue;
            }

            if (commandRegistry.isCommand(userInput, commands.getCommands())) {
                executeCommand(userInput);
                continue;
            }

            handleUserMessage(userInput);
        }
    }

    private void executeCommand(String userInput) {

        try {
            ConsoleCommandHandler handler = commandRegistry.getHandler(userInput);

            if (handler == null) {
                throw new IllegalStateException("No handler registered for command: " + userInput);
            }

            handler.execute(commandContext);

        } catch (Exception e) {
            System.out.println(messages.getErrorPrefix() + e.getMessage());
        }
    }

    private void printWelcome() {
        System.out.println(messages.getWelcomeMessage());
        System.out.println(messages.getHelpMessage());
    }

    private void handleUserMessage(String userMessage) {
        try {
            RoutingPlan routingPlan = routerService.route(userMessage);

            System.out.println();
            System.out.println(messages.getRoutingPlanHeader());
            System.out.println(routingPlan);
            System.out.println();
        } catch (Exception e) {
            System.out.println(messages.getErrorPrefix() + e.getMessage());
        }
    }
}