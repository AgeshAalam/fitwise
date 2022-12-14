package com.fitwise.repository.qbo;

import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.qbo.QboBillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing vendor credit qbo entity in fitwise
 */
@Repository
public interface QboBillPaymentRepository extends JpaRepository<QboBillPayment, Long> {

    List<QboBillPayment> findByOrderManagement(final OrderManagement orderManagement);

    List<QboBillPayment> findByNeedUpdate(final Boolean status);
}
