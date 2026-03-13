package org.example.billing;

import org.example.billing.model.Customer;
import org.example.billing.model.Subscription;
import org.example.database.DatabaseConnectionFactory;
import org.example.database.DatabaseInitializer;
import org.example.database.PostgresContainerManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionRepositoryTest {

    private static PostgresContainerManager postgresContainerManager;
    private static DatabaseConnectionFactory connectionFactory;
    private static CustomerRepository customerRepository;
    private static SubscriptionRepository subscriptionRepository;

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

        customerRepository = new CustomerRepository(connectionFactory);
        subscriptionRepository = new SubscriptionRepository(connectionFactory);
    }

    @AfterAll
    static void tearDown() {
        if (postgresContainerManager != null) {
            postgresContainerManager.stop();
        }
    }

    @Test
    void shouldFindActiveSubscriptionByCustomerId() {
        Customer john = customerRepository.findCustomerByEmail("john.doe@example.com")
                .orElseThrow(() -> new AssertionError("John Doe should exist in test database"));

        Optional<Subscription> result = subscriptionRepository.findActiveSubscriptionByCustomerId(john.getId());

        assertTrue(result.isPresent());

        Subscription subscription = result.get();
        assertEquals(john.getId(), subscription.getCustomerId());
        assertEquals("PRO", subscription.getPlanName());
        assertEquals(0, new BigDecimal("29.99").compareTo(subscription.getMonthlyPrice()));
        assertEquals("ACTIVE", subscription.getStatus());
        assertNotNull(subscription.getStartedAt());
        assertTrue(subscription.getId() > 0);
    }

    @Test
    void shouldReturnEmptyWhenCustomerHasNoActiveSubscription() {
        Optional<Subscription> result = subscriptionRepository.findActiveSubscriptionByCustomerId(-999);

        assertTrue(result.isEmpty());
    }
}