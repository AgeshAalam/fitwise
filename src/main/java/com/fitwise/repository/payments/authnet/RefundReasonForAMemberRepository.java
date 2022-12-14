package com.fitwise.repository.payments.authnet;

import com.fitwise.entity.payments.common.RefundReasonForAMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundReasonForAMemberRepository extends JpaRepository<RefundReasonForAMember, Long> {
    RefundReasonForAMember findByTransactionId(String transactionId);
}
