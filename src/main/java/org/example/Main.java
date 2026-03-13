package org.example;

import org.example.agent.AgentRegistry;
import org.example.agent.PlanStepExecutor;
import org.example.agent.impl.BillingSpecialistAgent;
import org.example.agent.impl.GeneralAgent;
import org.example.agent.impl.TechnicalSpecialistAgent;
import org.example.billing.BillingService;
import org.example.billing.CustomerRepository;
import org.example.billing.RefundPolicyRepository;
import org.example.billing.SubscriptionRepository;
import org.example.billing.SupportCaseRepository;
import org.example.conversation.ConversationHistory;
import org.example.database.DatabaseConnectionFactory;
import org.example.database.DatabaseInitializer;
import org.example.database.PostgresContainerManager;
import org.example.llm.LlmClient;
import org.example.router.RouterService;
import org.example.router.UnknownResolutionService;
import org.example.technicaldocs.CosineSimilarityCalculator;
import org.example.technicaldocs.EmbeddingClient;
import org.example.technicaldocs.TechnicalChunkEmbeddingService;
import org.example.technicaldocs.TechnicalChunkRetriever;
import org.example.technicaldocs.TechnicalDocumentChunker;
import org.example.technicaldocs.TechnicalDocumentLoader;
import org.example.technicaldocs.TechnicalDocumentationService;
import org.example.technicaldocs.config.TechnicalDocsConfig;
import org.example.ui.ConsoleChatApplication;
import org.example.ui.ConsoleCommands;
import org.example.ui.ConsoleMessages;
import org.example.ui.command.ConsoleCommandRegistry;
import org.example.ui.command.context.ConsoleCommandContext;
import org.example.ui.command.impl.AgentsCommand;
import org.example.ui.command.impl.ExitCommand;
import org.example.ui.command.impl.HelpCommand;
import org.example.ui.command.impl.HistoryCommand;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        PostgresContainerManager postgresContainerManager = new PostgresContainerManager();

        try {
            postgresContainerManager.start();

            DatabaseInitializer databaseInitializer = new DatabaseInitializer(
                    postgresContainerManager.getJdbcUrl(),
                    postgresContainerManager.getUsername(),
                    postgresContainerManager.getPassword()
            );
            databaseInitializer.initialize();

            DatabaseConnectionFactory databaseConnectionFactory = new DatabaseConnectionFactory(
                    postgresContainerManager.getJdbcUrl(),
                    postgresContainerManager.getUsername(),
                    postgresContainerManager.getPassword()
            );

            CustomerRepository customerRepository = new CustomerRepository(databaseConnectionFactory);
            SubscriptionRepository subscriptionRepository = new SubscriptionRepository(databaseConnectionFactory);
            RefundPolicyRepository refundPolicyRepository = new RefundPolicyRepository(databaseConnectionFactory);
            SupportCaseRepository supportCaseRepository = new SupportCaseRepository(databaseConnectionFactory);

            BillingService billingService = new BillingService(
                    customerRepository,
                    subscriptionRepository,
                    refundPolicyRepository,
                    supportCaseRepository
            );

            ConsoleMessages messages = ConsoleMessages.load();
            ConsoleCommands commands = ConsoleCommands.load();
            ConversationHistory history = new ConversationHistory();

            try (Scanner scanner = new Scanner(System.in)) {
                LlmClient llmClient = new LlmClient();
                RouterService routerService = new RouterService(llmClient);
                UnknownResolutionService unknownResolutionService =
                        new UnknownResolutionService(llmClient, routerService);

                TechnicalDocsConfig technicalDocsConfig = TechnicalDocsConfig.load();
                TechnicalDocumentLoader technicalDocumentLoader =
                        new TechnicalDocumentLoader(technicalDocsConfig);
                TechnicalDocumentChunker technicalDocumentChunker =
                        new TechnicalDocumentChunker(technicalDocsConfig);
                EmbeddingClient embeddingClient = new EmbeddingClient(technicalDocsConfig);
                TechnicalChunkEmbeddingService technicalChunkEmbeddingService =
                        new TechnicalChunkEmbeddingService(technicalDocumentChunker, embeddingClient);
                TechnicalChunkRetriever technicalChunkRetriever =
                        new TechnicalChunkRetriever(new CosineSimilarityCalculator());
                TechnicalDocumentationService technicalDocumentationService =
                        new TechnicalDocumentationService(
                                technicalDocumentLoader,
                                technicalChunkEmbeddingService,
                                technicalChunkRetriever,
                                embeddingClient
                        );

                technicalDocumentationService.initialize();

                AgentRegistry agentRegistry = new AgentRegistry(
                        List.of(
                                new BillingSpecialistAgent(llmClient, billingService),
                                new TechnicalSpecialistAgent(llmClient, technicalDocumentationService),
                                new GeneralAgent(llmClient)
                        )
                );

                PlanStepExecutor planStepExecutor = new PlanStepExecutor(agentRegistry);

                ConsoleCommandContext commandContext = new ConsoleCommandContext(
                        messages,
                        commands,
                        history
                );

                ConsoleCommandRegistry commandRegistry = new ConsoleCommandRegistry(
                        List.of(
                                new HelpCommand(),
                                new ExitCommand(),
                                new AgentsCommand(),
                                new HistoryCommand()
                        ),
                        commands.getCommands()
                );

                ConsoleChatApplication application = new ConsoleChatApplication(
                        routerService,
                        messages,
                        commands,
                        commandRegistry,
                        commandContext,
                        scanner,
                        unknownResolutionService,
                        history,
                        planStepExecutor
                );

                application.run();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                postgresContainerManager.stop();
            } catch (Exception ignored) {
            }
        }
    }
}