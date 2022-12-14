package com.fitwise.repository.payments.credits;

import com.fitwise.entity.payments.credits.InstructorPaymentCreditHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 22/01/21
 */
@Repository
public interface InstructorPaymentCreditHistoryRepository extends JpaRepository<InstructorPaymentCreditHistory, Long> {
}
