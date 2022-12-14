package com.fitwise.repository.packaging;

import com.fitwise.entity.packaging.PackageExternalClientMapping;
import com.fitwise.entity.packaging.SubscriptionPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 06/10/20
 */
@Repository
public interface PackageExternalClientMappingRepository  extends JpaRepository<PackageExternalClientMapping, Long> {

    /**
     * @param subscriptionPackage
     * @return
     */
    List<PackageExternalClientMapping> findBySubscriptionPackage(SubscriptionPackage subscriptionPackage);

    PackageExternalClientMapping findTop1BySubscriptionPackageSubscriptionPackageIdAndExternalClientEmail(Long subscriptionPackageId, String email);

    List<PackageExternalClientMapping> findByExternalClientEmail(String email);
}
