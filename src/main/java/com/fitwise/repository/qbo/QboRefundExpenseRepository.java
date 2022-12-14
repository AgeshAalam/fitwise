package com.fitwise.repository.qbo;

import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.qbo.QboRefundExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing vendor credit qbo entity in fitwise
 */
@Repository
public interface QboRefundExpenseRepository extends JpaRepository<QboRefundExpense, Long> {

    List<QboRefundExpense> findByOrderManagement(final OrderManagement orderManagement);

    List<QboRefundExpense> findByNeedUpdate(final Boolean status);
}
