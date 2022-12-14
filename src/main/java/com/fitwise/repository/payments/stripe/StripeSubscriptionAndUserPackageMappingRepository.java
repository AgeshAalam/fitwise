package com.fitwise.repository.payments.stripe;

import com.fitwise.entity.payments.stripe.StripeSubscriptionAndUserPackageMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 18/12/20
 */
@Repository
public interface StripeSubscriptionAndUserPackageMappingRepository extends JpaRepository<StripeSubscriptionAndUserPackageMapping, Long> {

    /**
     * @param userId
     * @param packageId
     * @return
     */
    StripeSubscriptionAndUserPackageMapping findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageId(Long userId, Long packageId);

    /**
     * @param userId
     * @param packageId
     * @return
     */
    StripeSubscriptionAndUserPackageMapping findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderByModifiedDateDesc(Long userId, Long packageId);

    /**
     * @param userId
     * @param packageId
     * @param subscriptionStatus
     * @return
     */
    List<StripeSubscriptionAndUserPackageMapping> findByUserUserIdAndSubscriptionPackageSubscriptionPackageIdAndSubscriptionStatusSubscriptionStatus(Long userId, Long packageId, String subscriptionStatus);

    /**
     * @param stripeSubscriptionId
     * @return
     */
    StripeSubscriptionAndUserPackageMapping findTop1ByStripeSubscriptionId(String stripeSubscriptionId);

    /**
     * @param stripeSubscriptionId
     * @return
     */
    boolean existsByStripeSubscriptionId(String stripeSubscriptionId);

}
