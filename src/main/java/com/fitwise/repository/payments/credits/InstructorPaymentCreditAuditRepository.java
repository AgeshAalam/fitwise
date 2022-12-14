package com.fitwise.repository.payments.credits;

import com.fitwise.entity.User;
import com.fitwise.entity.payments.credits.InstructorPaymentCreditAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 13/01/21
 */
@Repository
public interface InstructorPaymentCreditAuditRepository extends JpaRepository<InstructorPaymentCreditAudit, Long> {

    /**
     * @param instructor
     * @param isCreditSettled
     * @return
     */
    List<InstructorPaymentCreditAudit> findByInstructorPaymentCreditsInstructorAndIsCreditAndIsCreditSettled(User instructor, boolean isCredit, boolean isCreditSettled);

}
