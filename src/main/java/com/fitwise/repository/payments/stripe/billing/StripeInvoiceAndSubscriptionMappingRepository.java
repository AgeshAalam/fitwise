package com.fitwise.repository.payments.stripe.billing;

import com.fitwise.entity.payments.stripe.billing.StripeInvoiceAndSubscriptionMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeInvoiceAndSubscriptionMappingRepository extends JpaRepository<StripeInvoiceAndSubscriptionMapping, Long> {
    StripeInvoiceAndSubscriptionMapping findByStripeInvoiceId(String stripeInvoiceId);
}
