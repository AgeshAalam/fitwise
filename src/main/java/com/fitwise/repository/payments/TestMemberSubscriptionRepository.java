package com.fitwise.repository.payments;

import com.fitwise.entity.payments.authNet.TestMemberSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 02/09/20
 */
@Repository
public interface TestMemberSubscriptionRepository extends JpaRepository<TestMemberSubscription, Long> {
}
