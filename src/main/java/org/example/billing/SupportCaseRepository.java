package org.example.billing;

import org.example.billing.model.SupportCase;
import org.example.database.DatabaseConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SupportCaseRepository {

    private final DatabaseConnectionFactory connectionFactory;

    public SupportCaseRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public SupportCase createRefundSupportCase(int customerId, String description) {
        String caseNumber = generateCaseNumber();

        String sql = """
                INSERT INTO support_cases (
                    case_number,
                    customer_id,
                    case_type,
                    status,
                    description
                )
                VALUES (?, ?, ?, ?, ?)
                RETURNING id, case_number, customer_id, case_type, status, description, created_at
                """;

        try (Connection connection = connectionFactory.createConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, caseNumber);
            statement.setInt(2, customerId);
            statement.setString(3, "REFUND");
            statement.setString(4, "OPEN");
            statement.setString(5, description);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Failed to create refund support case");
                }

                return new SupportCase(
                        resultSet.getInt("id"),
                        resultSet.getString("case_number"),
                        resultSet.getInt("customer_id"),
                        resultSet.getString("case_type"),
                        resultSet.getString("status"),
                        resultSet.getString("description"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()
                );
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create refund support case", e);
        }
    }

    private String generateCaseNumber() {
        return "REFUND-" + System.currentTimeMillis();
    }
}