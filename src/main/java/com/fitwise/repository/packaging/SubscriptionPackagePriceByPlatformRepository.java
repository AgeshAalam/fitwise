package com.fitwise.repository.packaging;

import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.packaging.SubscriptionPackagePriceByPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 24/09/20
 */
@Repository
public interface SubscriptionPackagePriceByPlatformRepository extends JpaRepository<SubscriptionPackagePriceByPlatform, Long> {

    /**
     * @param subscriptionPackage
     * @return
     */
    List<SubscriptionPackagePriceByPlatform> findBySubscriptionPackage(SubscriptionPackage subscriptionPackage);


}
