package com.fitwise.repository;

import com.fitwise.entity.InstructorOutstandingPayment;
import com.fitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh.G on 07/07/21
 */
@Repository
public interface InstructorOutstandingPaymentRepository extends JpaRepository<InstructorOutstandingPayment, Long> {
    /**
     * @param instructor
     * @return
     */
    InstructorOutstandingPayment findByUser(User instructor);
}
