package com.fitwise.repository.payments.stripe.billing;

import com.fitwise.entity.payments.stripe.billing.StripePayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripePayoutRepository extends JpaRepository<StripePayout, Long> {
}
