package org.example.billing;

import org.example.billing.model.Customer;
import org.example.billing.model.SupportCase;
import org.example.database.DatabaseConnectionFactory;
import org.example.database.DatabaseInitializer;
import org.example.database.PostgresContainerManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SupportCaseRepositoryTest {

    private static PostgresContainerManager postgresContainerManager;
    private static DatabaseConnectionFactory connectionFactory;
    private static CustomerRepository customerRepository;
    private static SupportCaseRepository supportCaseRepository;

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
        supportCaseRepository = new SupportCaseRepository(connectionFactory);
    }

    @AfterAll
    static void tearDown() {
        if (postgresContainerManager != null) {
            postgresContainerManager.stop();
        }
    }

    @Test
    void shouldCreateRefundSupportCase() {
        Customer john = customerRepository.findCustomerByEmail("john.doe@example.com")
                .orElseThrow(() -> new AssertionError("John Doe should exist in test database"));

        String description = "Customer requests refund because of accidental purchase.";

        SupportCase supportCase = supportCaseRepository.createRefundSupportCase(john.getId(), description);

        assertNotNull(supportCase);
        assertTrue(supportCase.getId() > 0);
        assertNotNull(supportCase.getCaseNumber());
        assertTrue(supportCase.getCaseNumber().startsWith("REFUND-"));
        assertEquals(john.getId(), supportCase.getCustomerId());
        assertEquals("REFUND", supportCase.getCaseType());
        assertEquals("OPEN", supportCase.getStatus());
        assertEquals(description, supportCase.getDescription());
        assertNotNull(supportCase.getCreatedAt());
    }
}