package org.example.billing;

import org.example.billing.model.Customer;
import org.example.database.DatabaseConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class CustomerRepository {

    private final DatabaseConnectionFactory connectionFactory;

    public CustomerRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Optional<Customer> findCustomerByEmail(String email) {
        String sql = """
                SELECT id, email, full_name
                FROM customers
                WHERE email = ?
                """;

        try (Connection connection = connectionFactory.createConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                Customer customer = new Customer(
                        resultSet.getInt("id"),
                        resultSet.getString("email"),
                        resultSet.getString("full_name")
                );

                return Optional.of(customer);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to find customer by email: " + email, e);
        }
    }
}