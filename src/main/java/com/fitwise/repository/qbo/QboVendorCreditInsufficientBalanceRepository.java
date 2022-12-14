package com.fitwise.repository.qbo;

import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.payments.credits.InstructorPaymentCreditAudit;
import com.fitwise.entity.qbo.QboVendorCreditInsufficientBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Processing vendor credit qbo entity in fitwise
 */
@Repository
public interface QboVendorCreditInsufficientBalanceRepository extends JpaRepository<QboVendorCreditInsufficientBalance, Long> {

    List<QboVendorCreditInsufficientBalance> findByInstructorPaymentCreditAudit(final InstructorPaymentCreditAudit instructorPaymentCreditAudit);

    List<QboVendorCreditInsufficientBalance> findByInstructorPaymentCreditAuditInstructorPayment(final InstructorPayment instructorPayment);

    List<QboVendorCreditInsufficientBalance> findByNeedUpdate(final Boolean status);
}
