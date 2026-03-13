package org.example.billing.model;

public class RefundPolicy {

    private final int id;
    private final String planName;
    private final boolean refundAvailable;
    private final int refundWindowDays;
    private final int processingTimeDays;
    private final String policyDescription;

    public RefundPolicy(
            int id,
            String planName,
            boolean refundAvailable,
            int refundWindowDays,
            int processingTimeDays,
            String policyDescription
    ) {
        this.id = id;
        this.planName = planName;
        this.refundAvailable = refundAvailable;
        this.refundWindowDays = refundWindowDays;
        this.processingTimeDays = processingTimeDays;
        this.policyDescription = policyDescription;
    }

    public int getId() {
        return id;
    }

    public String getPlanName() {
        return planName;
    }

    public boolean isRefundAvailable() {
        return refundAvailable;
    }

    public int getRefundWindowDays() {
        return refundWindowDays;
    }

    public int getProcessingTimeDays() {
        return processingTimeDays;
    }

    public String getPolicyDescription() {
        return policyDescription;
    }
}