package com.fitwise.repository.payments.stripe.connect;

import com.fitwise.entity.payments.stripe.connect.StripeTransferErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeTransferErrorLogRepository extends JpaRepository<StripeTransferErrorLog, Long> {
    StripeTransferErrorLog findTop1ByInstructorPaymentInstructorPaymentId(Long instructorPaymentId);
}
