package com.fitwise.repository.qbo;

import com.fitwise.entity.InstructorPayment;
import com.fitwise.entity.qbo.QboBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Processing payment qbo entity in fitwise
 */
@Repository
public interface QboBillRepository extends JpaRepository<QboBill, Long> {

    List<QboBill> findByInstructorPayment(final InstructorPayment instructorPayment);

    List<QboBill> findByNeedUpdate(final Boolean status);

    List<QboBill> findByIsCCBillCreated(final Boolean status);

    List<QboBill> findByBillId(final String billId);

    List<QboBill> findByIsFixedCCBillCreated(final Boolean status);

    List<QboBill> findByNeedUpdateAndBillPaidAndInstructorPaymentDueDateLessThan(final Boolean updateStatus, final Boolean piadStatus, final Date dueDate);
}
