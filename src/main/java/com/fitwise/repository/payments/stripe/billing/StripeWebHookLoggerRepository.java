package com.fitwise.repository.payments.stripe.billing;

import com.fitwise.entity.payments.stripe.StripeWebHookLogger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 03/11/20
 */
@Repository
public interface StripeWebHookLoggerRepository  extends JpaRepository<StripeWebHookLogger, Long> {
}
