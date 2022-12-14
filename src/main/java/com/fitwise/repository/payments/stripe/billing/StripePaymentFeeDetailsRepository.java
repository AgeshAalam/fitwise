package com.fitwise.repository.payments.stripe.billing;

import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.payments.stripe.billing.StripePaymentFeeDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripePaymentFeeDetailsRepository extends JpaRepository<StripePaymentFeeDetails, Long> {

    StripePaymentFeeDetails findByStripePaymentAndType(final StripePayment stripePayment, final String type);
}
