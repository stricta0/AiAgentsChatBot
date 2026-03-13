package org.example.billing;

import org.example.billing.model.Customer;
import org.example.billing.model.RefundPolicy;
import org.example.billing.model.Subscription;
import org.example.billing.model.SupportCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BillingServiceTest {

    private CustomerRepository customerRepository;
    private SubscriptionRepository subscriptionRepository;
    private RefundPolicyRepository refundPolicyRepository;
    private SupportCaseRepository supportCaseRepository;

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        subscriptionRepository = mock(SubscriptionRepository.class);
        refundPolicyRepository = mock(RefundPolicyRepository.class);
        supportCaseRepository = mock(SupportCaseRepository.class);

        billingService = new BillingService(
                customerRepository,
                subscriptionRepository,
                refundPolicyRepository,
                supportCaseRepository
        );
    }

    @Test
    void shouldReturnCustomerPlanDetails() {
        Customer customer = new Customer(1, "john.doe@example.com", "John Doe");
        Subscription subscription = new Subscription(
                10,
                1,
                "PRO",
                new BigDecimal("29.99"),
                "ACTIVE",
                LocalDateTime.of(2026, 3, 1, 10, 0)
        );

        when(customerRepository.findCustomerByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(customer));
        when(subscriptionRepository.findActiveSubscriptionByCustomerId(1))
                .thenReturn(Optional.of(subscription));

        BillingService.CustomerPlanDetails result =
                billingService.getCustomerPlanDetails("john.doe@example.com");

        assertEquals(1, result.customerId());
        assertEquals("john.doe@example.com", result.email());
        assertEquals("John Doe", result.fullName());
        assertEquals("PRO", result.planName());
        assertEquals(0, new BigDecimal("29.99").compareTo(result.monthlyPrice()));
        assertEquals("ACTIVE", result.subscriptionStatus());
        assertEquals("2026-03-01T10:00", result.startedAt());

        verify(customerRepository).findCustomerByEmail("john.doe@example.com");
        verify(subscriptionRepository).findActiveSubscriptionByCustomerId(1);
    }

    @Test
    void shouldThrowWhenEmailIsBlankForPlanLookup() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> billingService.getCustomerPlanDetails(" ")
        );

        assertEquals("Email must not be null or blank", exception.getMessage());
        verifyNoInteractions(customerRepository, subscriptionRepository, refundPolicyRepository, supportCaseRepository);
    }

    @Test
    void shouldThrowWhenCustomerNotFoundForPlanLookup() {
        when(customerRepository.findCustomerByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> billingService.getCustomerPlanDetails("missing@example.com")
        );

        assertEquals("Customer not found for email: missing@example.com", exception.getMessage());
        verify(customerRepository).findCustomerByEmail("missing@example.com");
        verifyNoInteractions(subscriptionRepository, refundPolicyRepository, supportCaseRepository);
    }

    @Test
    void shouldThrowWhenActiveSubscriptionNotFoundForPlanLookup() {
        Customer customer = new Customer(1, "john.doe@example.com", "John Doe");

        when(customerRepository.findCustomerByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(customer));
        when(subscriptionRepository.findActiveSubscriptionByCustomerId(1))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> billingService.getCustomerPlanDetails("john.doe@example.com")
        );

        assertEquals("No active subscription found for customer email: john.doe@example.com", exception.getMessage());
        verify(customerRepository).findCustomerByEmail("john.doe@example.com");
        verify(subscriptionRepository).findActiveSubscriptionByCustomerId(1);
    }

    @Test
    void shouldReturnRefundPolicyDetails() {
        Customer customer = new Customer(1, "john.doe@example.com", "John Doe");
        Subscription subscription = new Subscription(
                10,
                1,
                "PRO",
                new BigDecimal("29.99"),
                "ACTIVE",
                LocalDateTime.of(2026, 3, 1, 10, 0)
        );
        RefundPolicy refundPolicy = new RefundPolicy(
                20,
                "PRO",
                true,
                14,
                3,
                "Pro plan can be refunded within 14 days of purchase."
        );

        when(customerRepository.findCustomerByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(customer));
        when(subscriptionRepository.findActiveSubscriptionByCustomerId(1))
                .thenReturn(Optional.of(subscription));
        when(refundPolicyRepository.findRefundPolicyByPlanName("PRO"))
                .thenReturn(Optional.of(refundPolicy));

        BillingService.CustomerRefundPolicyDetails result =
                billingService.getRefundPolicyDetails("john.doe@example.com");

        assertEquals(1, result.customerId());
        assertEquals("john.doe@example.com", result.email());
        assertEquals("John Doe", result.fullName());
        assertEquals("PRO", result.planName());
        assertTrue(result.refundAvailable());
        assertEquals(14, result.refundWindowDays());
        assertEquals(3, result.processingTimeDays());
        assertEquals("Pro plan can be refunded within 14 days of purchase.", result.policyDescription());

        verify(customerRepository).findCustomerByEmail("john.doe@example.com");
        verify(subscriptionRepository).findActiveSubscriptionByCustomerId(1);
        verify(refundPolicyRepository).findRefundPolicyByPlanName("PRO");
    }

    @Test
    void shouldThrowWhenRefundPolicyNotFound() {
        Customer customer = new Customer(1, "john.doe@example.com", "John Doe");
        Subscription subscription = new Subscription(
                10,
                1,
                "PRO",
                new BigDecimal("29.99"),
                "ACTIVE",
                LocalDateTime.of(2026, 3, 1, 10, 0)
        );

        when(customerRepository.findCustomerByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(customer));
        when(subscriptionRepository.findActiveSubscriptionByCustomerId(1))
                .thenReturn(Optional.of(subscription));
        when(refundPolicyRepository.findRefundPolicyByPlanName("PRO"))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> billingService.getRefundPolicyDetails("john.doe@example.com")
        );

        assertEquals("Refund policy not found for plan: PRO", exception.getMessage());
        verify(customerRepository).findCustomerByEmail("john.doe@example.com");
        verify(subscriptionRepository).findActiveSubscriptionByCustomerId(1);
        verify(refundPolicyRepository).findRefundPolicyByPlanName("PRO");
    }

    @Test
    void shouldOpenRefundSupportCase() {
        Customer customer = new Customer(1, "john.doe@example.com", "John Doe");
        SupportCase supportCase = new SupportCase(
                100,
                "REFUND-123456",
                1,
                "REFUND",
                "OPEN",
                "Customer wants a refund.",
                LocalDateTime.of(2026, 3, 13, 11, 30)
        );

        when(customerRepository.findCustomerByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(customer));
        when(supportCaseRepository.createRefundSupportCase(1, "Customer wants a refund."))
                .thenReturn(supportCase);

        BillingService.RefundSupportCaseResult result =
                billingService.openRefundSupportCase(
                        "john.doe@example.com",
                        "Customer wants a refund."
                );

        assertEquals(100, result.caseId());
        assertEquals("REFUND-123456", result.caseNumber());
        assertEquals(1, result.customerId());
        assertEquals("REFUND", result.caseType());
        assertEquals("OPEN", result.status());
        assertEquals("Customer wants a refund.", result.description());
        assertEquals("2026-03-13T11:30", result.createdAt());

        verify(customerRepository).findCustomerByEmail("john.doe@example.com");
        verify(supportCaseRepository).createRefundSupportCase(1, "Customer wants a refund.");
    }

    @Test
    void shouldTrimDescriptionBeforeCreatingRefundCase() {
        Customer customer = new Customer(1, "john.doe@example.com", "John Doe");
        SupportCase supportCase = new SupportCase(
                100,
                "REFUND-123456",
                1,
                "REFUND",
                "OPEN",
                "Customer wants a refund.",
                LocalDateTime.of(2026, 3, 13, 11, 30)
        );

        when(customerRepository.findCustomerByEmail("john.doe@example.com"))
                .thenReturn(Optional.of(customer));
        when(supportCaseRepository.createRefundSupportCase(1, "Customer wants a refund."))
                .thenReturn(supportCase);

        billingService.openRefundSupportCase(
                "john.doe@example.com",
                "   Customer wants a refund.   "
        );

        verify(supportCaseRepository).createRefundSupportCase(1, "Customer wants a refund.");
    }

    @Test
    void shouldThrowWhenDescriptionIsBlankForRefundCase() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> billingService.openRefundSupportCase("john.doe@example.com", "   ")
        );

        assertEquals("Description must not be null or blank", exception.getMessage());
        verifyNoInteractions(customerRepository, subscriptionRepository, refundPolicyRepository, supportCaseRepository);
    }

    @Test
    void shouldThrowWhenCustomerNotFoundForRefundCase() {
        when(customerRepository.findCustomerByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> billingService.openRefundSupportCase("missing@example.com", "Refund request")
        );

        assertEquals("Customer not found for email: missing@example.com", exception.getMessage());
        verify(customerRepository).findCustomerByEmail("missing@example.com");
        verifyNoInteractions(subscriptionRepository, refundPolicyRepository, supportCaseRepository);
    }
}