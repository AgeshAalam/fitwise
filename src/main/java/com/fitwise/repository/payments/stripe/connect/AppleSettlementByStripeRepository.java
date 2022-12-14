package com.fitwise.repository.payments.stripe.connect;

import com.fitwise.entity.payments.stripe.connect.AppleSettlementByStripe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppleSettlementByStripeRepository extends JpaRepository<AppleSettlementByStripe, Long> {
    AppleSettlementByStripe findTop1ByStripeTopUpId(String stripeTopUpId);


}
