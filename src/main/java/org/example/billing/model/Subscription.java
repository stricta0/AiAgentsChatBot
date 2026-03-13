package org.example.billing.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Subscription {

    private final int id;
    private final int customerId;
    private final String planName;
    private final BigDecimal monthlyPrice;
    private final String status;
    private final LocalDateTime startedAt;

    public Subscription(
            int id,
            int customerId,
            String planName,
            BigDecimal monthlyPrice,
            String status,
            LocalDateTime startedAt
    ) {
        this.id = id;
        this.customerId = customerId;
        this.planName = planName;
        this.monthlyPrice = monthlyPrice;
        this.status = status;
        this.startedAt = startedAt;
    }

    public int getId() {
        return id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getPlanName() {
        return planName;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }
}