package org.example.billing.model;

import java.time.LocalDateTime;

public class SupportCase {

    private final int id;
    private final String caseNumber;
    private final int customerId;
    private final String caseType;
    private final String status;
    private final String description;
    private final LocalDateTime createdAt;

    public SupportCase(
            int id,
            String caseNumber,
            int customerId,
            String caseType,
            String status,
            String description,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.caseNumber = caseNumber;
        this.customerId = customerId;
        this.caseType = caseType;
        this.status = status;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getCaseType() {
        return caseType;
    }

    public String getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}