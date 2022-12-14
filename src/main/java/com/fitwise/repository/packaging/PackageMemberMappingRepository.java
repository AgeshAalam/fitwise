package com.fitwise.repository.packaging;

import com.fitwise.entity.User;
import com.fitwise.entity.packaging.SubscriptionPackage;
import com.fitwise.entity.packaging.PackageMemberMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 25/09/20
 */
@Repository
public interface PackageMemberMappingRepository extends JpaRepository<PackageMemberMapping, Long> {

    /**
     * @param subscriptionPackage
     * @return
     */
    List<PackageMemberMapping> findBySubscriptionPackage(SubscriptionPackage subscriptionPackage);

    PackageMemberMapping findTop1BySubscriptionPackageSubscriptionPackageIdAndUserUserId(Long subscriptionPackageId, Long userId);

    List<PackageMemberMapping> findByUser(User user);

}
