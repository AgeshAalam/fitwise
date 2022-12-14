package com.fitwise.repository.qbo;

import com.fitwise.entity.payments.credits.InstructorPaymentCreditAudit;
import com.fitwise.entity.qbo.QboExpenseInsuffiecientBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing expense for vendor insufficient balance
 */
@Repository
public interface QboExpenseInsufficientBalanceRepository extends JpaRepository<QboExpenseInsuffiecientBalance, Long> {

    List<QboExpenseInsuffiecientBalance> findByInstructorPaymentCreditAudit(final InstructorPaymentCreditAudit instructorPaymentCreditAudit);

    List<QboExpenseInsuffiecientBalance> findByNeedUpdate(final Boolean status);
}
