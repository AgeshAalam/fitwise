package com.fitwise.repository;

import com.fitwise.entity.PackageAccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageAccessTokenRepository extends JpaRepository<PackageAccessToken,Long> {

    PackageAccessToken findBySubscriptionPackageSubscriptionPackageIdAndAccessToken(Long subscriptionPackageId, String token);
}
