package com.fitwise.repository.payments.stripe.billing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionAndUserTierMapping;

@Repository
public interface StripeSubscriptionAndUserTierMappingRepository extends JpaRepository<StripeSubscriptionAndUserTierMapping, Long>{
    boolean existsByStripeSubscriptionId(String stripeSubscriptionId);
    StripeSubscriptionAndUserTierMapping findTop1ByStripeSubscriptionId(String stripeSubscriptionId);
}
