package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.agent.AgentCatalog;
import org.example.agent.AgentRegistry;
import org.example.agent.PlanStepExecutor;
import org.example.agent.impl.BillingSpecialistAgent;
import org.example.agent.impl.GeneralAgent;
import org.example.agent.impl.TechnicalSpecialistAgent;
import org.example.agent.prompt.BillingSpecialistErrorPromptDefinition;
import org.example.agent.prompt.BillingSpecialistErrorPromptFactory;
import org.example.agent.prompt.BillingSpecialistPromptDefinition;
import org.example.agent.prompt.BillingSpecialistPromptFactory;
import org.example.agent.prompt.TechnicalSpecialistPromptDefinition;
import org.example.agent.prompt.TechnicalSpecialistPromptFactory;
import org.example.billing.BillingService;
import org.example.billing.CustomerRepository;
import org.example.billing.RefundPolicyRepository;
import org.example.billing.SubscriptionRepository;
import org.example.billing.SupportCaseRepository;
import org.example.config.JsonResourceLoader;
import org.example.config.ResourcePaths;
import org.example.conversation.ConversationHistory;
import org.example.database.DatabaseConnectionFactory;
import org.example.database.DatabaseInitializer;
import org.example.database.PostgresContainerManager;
import org.example.llm.LlmClient;
import org.example.router.RouterPromptFactory;
import org.example.router.RouterService;
import org.example.router.UnknownResolutionPromptFactory;
import org.example.router.UnknownResolutionService;
import org.example.router.model.RouterPromptDefinition;
import org.example.router.model.UnknownResolutionPromptDefinition;
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

            ConversationHistory history = new ConversationHistory();

            try (Scanner scanner = new Scanner(System.in)) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonResourceLoader jsonResourceLoader = new JsonResourceLoader(objectMapper);

                AgentCatalog agentCatalog =
                        jsonResourceLoader.load(ResourcePaths.AGENTS_CATALOG, AgentCatalog.class);

                RouterPromptDefinition routerPromptDefinition =
                        jsonResourceLoader.load(ResourcePaths.ROUTER_PROMPT, RouterPromptDefinition.class);

                UnknownResolutionPromptDefinition unknownResolutionPromptDefinition =
                        jsonResourceLoader.load(
                                ResourcePaths.UNKNOWN_RESOLUTION_PROMPT,
                                UnknownResolutionPromptDefinition.class
                        );

                BillingSpecialistPromptDefinition billingPromptDefinition =
                        jsonResourceLoader.load(
                                ResourcePaths.BILLING_SPECIALIST_PROMPT,
                                BillingSpecialistPromptDefinition.class
                        );

                BillingSpecialistErrorPromptDefinition billingErrorPromptDefinition =
                        jsonResourceLoader.load(
                                ResourcePaths.BILLING_SPECIALIST_ERROR_PROMPT,
                                BillingSpecialistErrorPromptDefinition.class
                        );

                TechnicalSpecialistPromptDefinition technicalPromptDefinition =
                        jsonResourceLoader.load(
                                ResourcePaths.TECHNICAL_SPECIALIST_PROMPT,
                                TechnicalSpecialistPromptDefinition.class
                        );

                ConsoleMessages messages =
                        jsonResourceLoader.load(ResourcePaths.CONSOLE_MESSAGES, ConsoleMessages.class);

                ConsoleCommands commands =
                        jsonResourceLoader.load(ResourcePaths.CONSOLE_COMMANDS, ConsoleCommands.class);

                TechnicalDocsConfig technicalDocsConfig =
                        jsonResourceLoader.load(ResourcePaths.TECHNICAL_DOCS_CONFIG, TechnicalDocsConfig.class);

                RouterPromptFactory routerPromptFactory = new RouterPromptFactory();
                UnknownResolutionPromptFactory unknownResolutionPromptFactory =
                        new UnknownResolutionPromptFactory();
                BillingSpecialistPromptFactory billingPromptFactory =
                        new BillingSpecialistPromptFactory();
                BillingSpecialistErrorPromptFactory billingErrorPromptFactory =
                        new BillingSpecialistErrorPromptFactory();
                TechnicalSpecialistPromptFactory technicalSpecialistPromptFactory =
                        new TechnicalSpecialistPromptFactory();

                LlmClient llmClient = new LlmClient();

                RouterService routerService = new RouterService(
                        llmClient,
                        routerPromptFactory,
                        objectMapper,
                        agentCatalog,
                        routerPromptDefinition
                );

                UnknownResolutionService unknownResolutionService =
                        new UnknownResolutionService(
                                llmClient,
                                routerService,
                                objectMapper,
                                agentCatalog,
                                routerPromptDefinition,
                                unknownResolutionPromptDefinition,
                                unknownResolutionPromptFactory
                        );

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
                                new BillingSpecialistAgent(
                                        llmClient,
                                        billingService,
                                        objectMapper,
                                        billingPromptDefinition,
                                        billingPromptFactory,
                                        billingErrorPromptDefinition,
                                        billingErrorPromptFactory
                                ),
                                new TechnicalSpecialistAgent(
                                        llmClient,
                                        technicalDocumentationService,
                                        objectMapper,
                                        technicalPromptDefinition,
                                        technicalSpecialistPromptFactory
                                ),
                                new GeneralAgent(llmClient)
                        ),
                        agentCatalog
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
                                new AgentsCommand(agentCatalog),
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