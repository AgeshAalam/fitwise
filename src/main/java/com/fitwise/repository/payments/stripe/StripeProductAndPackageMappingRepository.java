package com.fitwise.repository.payments.stripe;

import com.fitwise.entity.payments.stripe.StripeProductAndPackageMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 18/12/20
 */
@Repository
public interface StripeProductAndPackageMappingRepository extends JpaRepository<StripeProductAndPackageMapping, Long> {

    /**
     * @param subscriptionPackageId
     * @param isActive
     * @return
     */
    StripeProductAndPackageMapping findBySubscriptionPackageSubscriptionPackageIdAndIsActive(Long subscriptionPackageId, boolean isActive);

    /**
     * @param subscriptionPackageId
     * @return
     */
    StripeProductAndPackageMapping findBySubscriptionPackageSubscriptionPackageId(Long subscriptionPackageId);

}
