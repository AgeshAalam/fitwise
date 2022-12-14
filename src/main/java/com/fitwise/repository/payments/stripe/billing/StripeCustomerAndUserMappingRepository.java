package com.fitwise.repository.payments.stripe.billing;

import com.fitwise.entity.payments.stripe.billing.StripeCustomerAndUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeCustomerAndUserMappingRepository extends JpaRepository<StripeCustomerAndUserMapping, Long> {
    StripeCustomerAndUserMapping findTop1ByUserUserId(Long userId);
}
