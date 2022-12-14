package com.fitwise.repository.payments.stripe.billing;


import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeSubscriptionStatusRepository extends JpaRepository<StripeSubscriptionStatus, Long> {
    StripeSubscriptionStatus findBySubscriptionStatusIgnoreCaseContaining(String subscriptionStatus);
}
