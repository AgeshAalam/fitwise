package com.fitwise.repository.payments.stripe.billing;

import com.fitwise.entity.payments.stripe.billing.StripeSubscriptionAndUserProgramMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StripeSubscriptionAndUserProgramMappingRepository extends JpaRepository<StripeSubscriptionAndUserProgramMapping, Long> {
    StripeSubscriptionAndUserProgramMapping findTop1ByUserUserIdAndProgramProgramId(Long userId, Long programId);

    /**
     * @param userId
     * @param programId
     * @return
     */
    StripeSubscriptionAndUserProgramMapping findTop1ByUserUserIdAndProgramProgramIdOrderByModifiedDateDesc(Long userId, Long programId);

    /**
     * @param userId
     * @param programId
     * @param subscriptionStatus
     * @return
     */
    List<StripeSubscriptionAndUserProgramMapping> findByUserUserIdAndProgramProgramIdAndSubscriptionStatusSubscriptionStatus(Long userId, Long programId, String subscriptionStatus);

    /**
     * @param stripeSubscriptionId
     * @return
     */
    StripeSubscriptionAndUserProgramMapping findTop1ByStripeSubscriptionId(String stripeSubscriptionId);

    boolean existsByStripeSubscriptionId(String stripeSubscriptionId);

    List<StripeSubscriptionAndUserProgramMapping> findByUserUserId(Long userId);

}
