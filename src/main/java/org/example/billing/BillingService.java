package org.example.billing;

import org.example.billing.model.Customer;
import org.example.billing.model.RefundPolicy;
import org.example.billing.model.Subscription;
import org.example.billing.model.SupportCase;

import java.math.BigDecimal;

public class BillingService {

    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RefundPolicyRepository refundPolicyRepository;
    private final SupportCaseRepository supportCaseRepository;

    public BillingService(
            CustomerRepository customerRepository,
            SubscriptionRepository subscriptionRepository,
            RefundPolicyRepository refundPolicyRepository,
            SupportCaseRepository supportCaseRepository
    ) {
        this.customerRepository = customerRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.refundPolicyRepository = refundPolicyRepository;
        this.supportCaseRepository = supportCaseRepository;
    }

    public CustomerPlanDetails getCustomerPlanDetails(String email) {
        validateEmail(email);

        Customer customer = findCustomerByEmailOrThrow(email);
        Subscription subscription = findActiveSubscriptionOrThrow(customer.getId(), email);

        return new CustomerPlanDetails(
                customer.getId(),
                customer.getEmail(),
                customer.getFullName(),
                subscription.getPlanName(),
                subscription.getMonthlyPrice(),
                subscription.getStatus(),
                subscription.getStartedAt().toString()
        );
    }

    public CustomerRefundPolicyDetails getRefundPolicyDetails(String email) {
        validateEmail(email);

        Customer customer = findCustomerByEmailOrThrow(email);
        Subscription subscription = findActiveSubscriptionOrThrow(customer.getId(), email);
        RefundPolicy refundPolicy = findRefundPolicyOrThrow(subscription.getPlanName());

        return new CustomerRefundPolicyDetails(
                customer.getId(),
                customer.getEmail(),
                customer.getFullName(),
                subscription.getPlanName(),
                refundPolicy.isRefundAvailable(),
                refundPolicy.getRefundWindowDays(),
                refundPolicy.getProcessingTimeDays(),
                refundPolicy.getPolicyDescription()
        );
    }

    public RefundSupportCaseResult openRefundSupportCase(String email, String description) {
        validateEmail(email);
        validateDescription(description);

        Customer customer = findCustomerByEmailOrThrow(email);
        SupportCase supportCase = supportCaseRepository.createRefundSupportCase(
                customer.getId(),
                description.trim()
        );

        return new RefundSupportCaseResult(
                supportCase.getId(),
                supportCase.getCaseNumber(),
                supportCase.getCustomerId(),
                supportCase.getCaseType(),
                supportCase.getStatus(),
                supportCase.getDescription(),
                supportCase.getCreatedAt().toString()
        );
    }

    private Customer findCustomerByEmailOrThrow(String email) {
        return customerRepository.findCustomerByEmail(email.trim())
                .orElseThrow(() -> new IllegalStateException(
                        "Customer not found for email: " + email
                ));
    }

    private Subscription findActiveSubscriptionOrThrow(int customerId, String email) {
        return subscriptionRepository.findActiveSubscriptionByCustomerId(customerId)
                .orElseThrow(() -> new IllegalStateException(
                        "No active subscription found for customer email: " + email
                ));
    }

    private RefundPolicy findRefundPolicyOrThrow(String planName) {
        return refundPolicyRepository.findRefundPolicyByPlanName(planName)
                .orElseThrow(() -> new IllegalStateException(
                        "Refund policy not found for plan: " + planName
                ));
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be null or blank");
        }
    }

    public record CustomerPlanDetails(
            int customerId,
            String email,
            String fullName,
            String planName,
            BigDecimal monthlyPrice,
            String subscriptionStatus,
            String startedAt
    ) {}

    public record CustomerRefundPolicyDetails(
            int customerId,
            String email,
            String fullName,
            String planName,
            boolean refundAvailable,
            int refundWindowDays,
            int processingTimeDays,
            String policyDescription
    ) {}

    public record RefundSupportCaseResult(
            int caseId,
            String caseNumber,
            int customerId,
            String caseType,
            String status,
            String description,
            String createdAt
    ) {}
}