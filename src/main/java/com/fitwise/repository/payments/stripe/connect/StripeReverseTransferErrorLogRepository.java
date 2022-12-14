package com.fitwise.repository.payments.stripe.connect;

import com.fitwise.entity.payments.stripe.connect.StripeReverseTransferErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 05/01/21
 */
@Repository
public interface StripeReverseTransferErrorLogRepository extends JpaRepository<StripeReverseTransferErrorLog, Long> {
}
