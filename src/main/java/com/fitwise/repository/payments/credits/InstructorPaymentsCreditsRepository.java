package com.fitwise.repository.payments.credits;

import com.fitwise.entity.payments.credits.InstructorPaymentCredits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstructorPaymentsCreditsRepository extends JpaRepository<InstructorPaymentCredits, Long> {

    /**
     * Since Credits may come for different types of currencies like $, INR, euro, Making the return as a list
     *
     * @param userId
     * @return
     */
    List<InstructorPaymentCredits> findByInstructorUserId(Long userId);

    /**
     * @param userId
     * @param currencyType
     * @return
     */
    InstructorPaymentCredits findByInstructorUserIdAndCurrencyType(Long userId, String currencyType);
}
