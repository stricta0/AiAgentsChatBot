package org.example.billing;

import org.example.billing.model.Customer;
import org.example.database.DatabaseConnectionFactory;
import org.example.database.DatabaseInitializer;
import org.example.database.PostgresContainerManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRepositoryTest {

    private static PostgresContainerManager postgresContainerManager;
    private static DatabaseConnectionFactory connectionFactory;
    private static CustomerRepository customerRepository;

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
    }

    @AfterAll
    static void tearDown() {
        if (postgresContainerManager != null) {
            postgresContainerManager.stop();
        }
    }

    @Test
    void shouldFindCustomerByEmail() {
        Optional<Customer> result = customerRepository.findCustomerByEmail("john.doe@example.com");

        assertTrue(result.isPresent());

        Customer customer = result.get();
        assertEquals("john.doe@example.com", customer.getEmail());
        assertEquals("John Doe", customer.getFullName());
        assertTrue(customer.getId() > 0);
    }

    @Test
    void shouldReturnEmptyWhenCustomerDoesNotExist() {
        Optional<Customer> result = customerRepository.findCustomerByEmail("missing@example.com");

        assertTrue(result.isEmpty());
    }
}