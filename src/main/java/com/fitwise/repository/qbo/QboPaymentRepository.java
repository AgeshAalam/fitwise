package com.fitwise.repository.qbo;

import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.qbo.QboPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing payment qbo entity in fitwise
 */
@Repository
public interface QboPaymentRepository extends JpaRepository<QboPayment, Long> {

    List<QboPayment> findByAuthNetPayment(final AuthNetPayment authNetPayment);

    List<QboPayment> findByApplePayment(final ApplePayment applePayment);

    List<QboPayment> findByStripePayment(final StripePayment stripePayment);

    List<QboPayment> findByNeedUpdate(final Boolean status);

    List<QboPayment> findByIsCCBillPaymentCreated(final Boolean status);

    List<QboPayment> findByisFixedCCBillPaymentCreated(final Boolean status);
}
