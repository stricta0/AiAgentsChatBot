package org.example.billing;

import org.example.billing.model.Subscription;
import org.example.database.DatabaseConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class SubscriptionRepository {

    private final DatabaseConnectionFactory connectionFactory;

    public SubscriptionRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Optional<Subscription> findActiveSubscriptionByCustomerId(int customerId) {
        String sql = """
                SELECT id, customer_id, plan_name, monthly_price, status, started_at
                FROM subscriptions
                WHERE customer_id = ? AND status = 'ACTIVE'
                ORDER BY started_at DESC
                LIMIT 1
                """;

        try (Connection connection = connectionFactory.createConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                Subscription subscription = new Subscription(
                        resultSet.getInt("id"),
                        resultSet.getInt("customer_id"),
                        resultSet.getString("plan_name"),
                        resultSet.getBigDecimal("monthly_price"),
                        resultSet.getString("status"),
                        resultSet.getTimestamp("started_at").toLocalDateTime()
                );

                return Optional.of(subscription);
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to find active subscription for customerId: " + customerId, e
            );
        }
    }
}