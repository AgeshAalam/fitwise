package com.fitwise.repository.qbo;

import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.qbo.QboBillPaymentForBillPaid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing vendor credit qbo entity in fitwise
 */
@Repository
public interface QboBillPaymentBillPaidRepository extends JpaRepository<QboBillPaymentForBillPaid, Long> {

    List<QboBillPaymentForBillPaid> findByOrderManagement(final OrderManagement orderManagement);

    List<QboBillPaymentForBillPaid> findByNeedUpdate(final Boolean status);
}
