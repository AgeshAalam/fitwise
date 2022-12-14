package com.fitwise.repository.subscription;

import com.fitwise.entity.subscription.InstructorSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InstructorSubscriptionRepo extends JpaRepository<InstructorSubscription, Long> {
    InstructorSubscription findByUserUserIdAndInstructorUserId(long userId, long instructor);

    //List<InstructorSubscription> findByProgramProgramIdAndIsAutoRenewal(Long programId, boolean b);
}