package com.fitwise.repository.qbo;

import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.qbo.QboBillPaymentInsufficientBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing vendor credit qbo entity in fitwise
 */
@Repository
public interface QboBillPaymentInsufficientBalanceRepository extends JpaRepository<QboBillPaymentInsufficientBalance, Long> {

    List<QboBillPaymentInsufficientBalance> findByOrderManagement(final OrderManagement orderManagement);

    List<QboBillPaymentInsufficientBalance> findByNeedUpdate(final Boolean status);

}