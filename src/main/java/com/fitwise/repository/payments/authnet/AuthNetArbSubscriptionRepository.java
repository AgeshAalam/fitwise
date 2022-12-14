package com.fitwise.repository.payments.authnet;

import com.fitwise.entity.payments.authNet.AuthNetArbSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AuthNetArbSubscriptionRepository extends JpaRepository<AuthNetArbSubscription, Long> {

    List<AuthNetArbSubscription> findByUserUserId(Long userId);

    List<AuthNetArbSubscription> findByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(Long userId, Long programId);

    AuthNetArbSubscription findTop1ByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(Long userId, Long programId);

    AuthNetArbSubscription findTop1ByUserUserIdAndProgramProgramIdAndCreatedDateLessThanEqualOrderByModifiedDateDesc(Long userId, Long programId, Date orderDate);

    AuthNetArbSubscription findTop1ByUserUserIdAndProgramProgramIdAndCreatedDateGreaterThanEqualOrderByModifiedDateAsc(Long userId, Long programId, Date orderDate);

    AuthNetArbSubscription findTop1ByUserUserIdAndProgramProgramIdAndCreatedDateLessThanEqual(Long userId, Long programId, Date orderDate);

    AuthNetArbSubscription findTop1ByUserUserIdAndProgramProgramId(Long userId, Long programId);

    AuthNetArbSubscription findTop1ByANetSubscriptionId(String authNetSubscriptionId);

    List<AuthNetArbSubscription> findByUserUserIdAndProgramProgramIdAndAuthNetSubscriptionStatusSubscriptionStatusName(Long userId, Long programId, String subscriptionStatus);

    List<AuthNetArbSubscription> findByUserUserIdAndProgramProgramIdAndAuthNetSubscriptionStatusSubscriptionStatusNameAndCreatedDateLessThanEqual(Long userId, Long programId, String subscriptionStatus, Date orderDate);
}
