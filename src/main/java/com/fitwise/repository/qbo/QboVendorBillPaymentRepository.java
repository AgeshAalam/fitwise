package com.fitwise.repository.qbo;

import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.qbo.QboBill;
import com.fitwise.entity.qbo.QboVendorBillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing vendor credit qbo entity in fitwise
 */
@Repository
public interface QboVendorBillPaymentRepository extends JpaRepository<QboVendorBillPayment, Long> {
    List<QboVendorBillPayment> findByQboBillPaymentId(final String billPaymentId);

    List<QboVendorBillPayment> findByQboBillInstructorPaymentOrderManagement(final OrderManagement orderManagement);

    List<QboVendorBillPayment> findByQboBill(final QboBill qboBill);
}
