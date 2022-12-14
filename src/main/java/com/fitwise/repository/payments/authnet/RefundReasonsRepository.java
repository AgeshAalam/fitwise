package com.fitwise.repository.payments.authnet;

import com.fitwise.entity.payments.common.RefundReasons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundReasonsRepository extends JpaRepository<RefundReasons, Long> {
    RefundReasons findByRefundReasonId(Long id);
}
