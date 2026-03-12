package org.example.ui;

import org.example.conversation.ConversationHistory;
import org.example.router.RouterService;
import org.example.router.UnknownResolutionService;
import org.example.router.model.RoutingPlan;
import org.example.ui.command.ConsoleCommandHandler;
import org.example.ui.command.ConsoleCommandRegistry;
import org.example.ui.command.context.ConsoleCommandContext;

import java.util.Optional;
import java.util.Scanner;

public class ConsoleChatApplication {

    private final RouterService routerService;
    private final ConsoleMessages messages;
    private final ConsoleCommands commands;
    private final ConsoleCommandRegistry commandRegistry;
    private final ConsoleCommandContext commandContext;
    private final Scanner scanner;
    private final UnknownResolutionService unknownResolutionService;
    private final ConversationHistory history;

    public ConsoleChatApplication(
            RouterService routerService,
            ConsoleMessages messages,
            ConsoleCommands commands,
            ConsoleCommandRegistry commandRegistry,
            ConsoleCommandContext commandContext,
            Scanner scanner,
            UnknownResolutionService unknownResolutionService,
            ConversationHistory history
    ) {
        this.routerService = routerService;
        this.messages = messages;
        this.commands = commands;
        this.commandRegistry = commandRegistry;
        this.commandContext = commandContext;
        this.scanner = scanner;
        this.unknownResolutionService = unknownResolutionService;
        this.history = history;
    }

    public void run() {
        printWelcome();

        while (!commandContext.isExitRequested()) {
            showToUser(messages.getNewPromptMessage());
            String userInput = getFromUser(messages.getPrompt());
            if (userInput.isBlank()) {
                showToUser(messages.getEmptyMessageWarning());
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

            String output = handler.execute(commandContext);

            if (output != null && !output.isBlank()) {
                showToUser(output);
            }

        } catch (Exception e) {
            showToUser(messages.getErrorPrefix() + e.getMessage());
        }
    }

    private void printWelcome() {
        String message = joinBlocks(
                messages.getWelcomeMessage(),
                messages.getHelpMessage()
        );
        showToUser(message);
    }

    private void handleUserMessage(String userMessage) {
        try {
            RoutingPlan routingPlan = routerService.route(userMessage, history);

            if (routingPlan.hasUnknownSteps()) {
                Optional<RoutingPlan> resolvedPlan = handleUnknownSteps(routingPlan);

                if (resolvedPlan.isEmpty()) {
                    return;
                }

                routingPlan = resolvedPlan.get();
            }

            String message = joinBlocks(
                    messages.getRoutingPlanHeader(),
                    routingPlan.toPrettyString()
            );
            showToUser(message);

        } catch (Exception e) {
            showToUser(messages.getErrorPrefix() + e.getMessage());
        }
    }

    private Optional<RoutingPlan> handleUnknownSteps(RoutingPlan routingPlan) {
        try {
            RoutingPlan currentPlan = routingPlan;

            while (currentPlan.hasUnknownSteps()) {
                String unknownMessage = joinBlocks(
                        messages.getUnknownPlanDetectedMessage(),
                        messages.getRoutingPlanHeader(),
                        currentPlan.toPrettyString(),
                        messages.getUnknownPlanOptionsMessage(),
                        messages.getUnknownPlanPrompt()
                );
                showToUser(unknownMessage);

                String userResolutionMessage = getFromUser(messages.getPrompt());

                if (userResolutionMessage.isBlank()) {
                    showToUser(messages.getEmptyMessageWarning());
                    continue;
                }

                Optional<RoutingPlan> resolvedPlan =
                        unknownResolutionService.resolve(currentPlan, userResolutionMessage, history);

                if (resolvedPlan.isEmpty()) {
                    return Optional.empty();
                }

                currentPlan = resolvedPlan.get();
            }

            return Optional.of(currentPlan);

        } catch (Exception e) {
            showToUser(messages.getErrorPrefix() + e.getMessage());
            return Optional.empty();
        }
    }

    private void showToUser(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        System.out.println();
        System.out.println(message);
        System.out.println();

        history.addAssistantMessage(message);
    }

    private String getFromUser(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        history.addUserMessage(input);
        return input;
    }

    private String joinBlocks(String... blocks) {
        StringBuilder sb = new StringBuilder();

        for (String block : blocks) {
            if (block == null || block.isBlank()) {
                continue;
            }

            if (!sb.isEmpty()) {
                sb.append("\n\n");
            }

            sb.append(block);
        }

        return sb.toString();
    }
}