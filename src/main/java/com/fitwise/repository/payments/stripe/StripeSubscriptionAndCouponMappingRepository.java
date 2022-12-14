package com.fitwise.repository.payments.stripe;

import com.fitwise.entity.payments.stripe.StripeSubscriptionAndCouponMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 24/11/20
 */
@Repository
public interface StripeSubscriptionAndCouponMappingRepository extends JpaRepository<StripeSubscriptionAndCouponMapping, Long> {

    /**
     * @param stripeSubscriptionId
     * @return
     */
    StripeSubscriptionAndCouponMapping findByStripeSubscriptionId(String stripeSubscriptionId);

}
