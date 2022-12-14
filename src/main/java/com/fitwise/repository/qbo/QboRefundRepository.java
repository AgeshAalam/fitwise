package com.fitwise.repository.qbo;

import com.fitwise.entity.payments.appleiap.ApplePayment;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import com.fitwise.entity.payments.stripe.billing.StripePayment;
import com.fitwise.entity.qbo.QboRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing refund qbo entity in fitwise
 */
@Repository
public interface QboRefundRepository extends JpaRepository<QboRefund, Long> {

    List<QboRefund> findByAuthNetPayment(final AuthNetPayment authNetPayment);

    List<QboRefund> findByApplePayment(final ApplePayment applePayment);

    List<QboRefund> findByStripePayment(final StripePayment stripePayment);

    List<QboRefund> findByNeedUpdate(final Boolean status);
}
