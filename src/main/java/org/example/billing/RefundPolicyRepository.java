package org.example.billing;

import org.example.billing.model.RefundPolicy;
import org.example.database.DatabaseConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class RefundPolicyRepository {

    private final DatabaseConnectionFactory connectionFactory;

    public RefundPolicyRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public Optional<RefundPolicy> findRefundPolicyByPlanName(String planName) {
        String sql = """
                SELECT id, plan_name, refund_available, refund_window_days, processing_time_days, policy_description
                FROM refund_policies
                WHERE plan_name = ?
                """;

        try (Connection connection = connectionFactory.createConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, planName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                RefundPolicy refundPolicy = new RefundPolicy(
                        resultSet.getInt("id"),
                        resultSet.getString("plan_name"),
                        resultSet.getBoolean("refund_available"),
                        resultSet.getInt("refund_window_days"),
                        resultSet.getInt("processing_time_days"),
                        resultSet.getString("policy_description")
                );

                return Optional.of(refundPolicy);
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to find refund policy for planName: " + planName, e
            );
        }
    }
}