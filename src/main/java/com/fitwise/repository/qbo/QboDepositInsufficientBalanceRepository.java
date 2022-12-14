package com.fitwise.repository.qbo;

import com.fitwise.entity.payments.common.OrderManagement;
import com.fitwise.entity.qbo.QboDepositInsufficientBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing qbo deposit for vendor insufficient balance
 */
@Repository
public interface QboDepositInsufficientBalanceRepository extends JpaRepository<QboDepositInsufficientBalance, Long> {

    List<QboDepositInsufficientBalance> findByOrderManagement(final OrderManagement orderManagement);

    List<QboDepositInsufficientBalance> findByNeedUpdate(final Boolean status);
}
