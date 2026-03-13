package org.example.billing;

import org.example.billing.model.RefundPolicy;
import org.example.database.DatabaseConnectionFactory;
import org.example.database.DatabaseInitializer;
import org.example.database.PostgresContainerManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RefundPolicyRepositoryTest {

    private static PostgresContainerManager postgresContainerManager;
    private static DatabaseConnectionFactory connectionFactory;
    private static RefundPolicyRepository refundPolicyRepository;

    @BeforeAll
    static void setUp() {
        postgresContainerManager = new PostgresContainerManager();
        postgresContainerManager.start();

        DatabaseInitializer databaseInitializer = new DatabaseInitializer(
                postgresContainerManager.getJdbcUrl(),
                postgresContainerManager.getUsername(),
                postgresContainerManager.getPassword()
        );
        databaseInitializer.initialize();

        connectionFactory = new DatabaseConnectionFactory(
                postgresContainerManager.getJdbcUrl(),
                postgresContainerManager.getUsername(),
                postgresContainerManager.getPassword()
        );

        refundPolicyRepository = new RefundPolicyRepository(connectionFactory);
    }

    @AfterAll
    static void tearDown() {
        if (postgresContainerManager != null) {
            postgresContainerManager.stop();
        }
    }

    @Test
    void shouldFindRefundPolicyForProPlan() {
        Optional<RefundPolicy> result = refundPolicyRepository.findRefundPolicyByPlanName("PRO");

        assertTrue(result.isPresent());

        RefundPolicy policy = result.get();
        assertEquals("PRO", policy.getPlanName());
        assertTrue(policy.isRefundAvailable());
        assertEquals(14, policy.getRefundWindowDays());
        assertEquals(3, policy.getProcessingTimeDays());
        assertEquals("Pro plan can be refunded within 14 days of purchase.", policy.getPolicyDescription());
        assertTrue(policy.getId() > 0);
    }

    @Test
    void shouldFindRefundPolicyForBasicPlan() {
        Optional<RefundPolicy> result = refundPolicyRepository.findRefundPolicyByPlanName("BASIC");

        assertTrue(result.isPresent());

        RefundPolicy policy = result.get();
        assertEquals("BASIC", policy.getPlanName());
        assertTrue(policy.isRefundAvailable());
        assertEquals(7, policy.getRefundWindowDays());
        assertEquals(5, policy.getProcessingTimeDays());
    }

    @Test
    void shouldReturnEmptyWhenPlanDoesNotExist() {
        Optional<RefundPolicy> result = refundPolicyRepository.findRefundPolicyByPlanName("UNKNOWN_PLAN");

        assertTrue(result.isEmpty());
    }
}