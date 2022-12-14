package com.fitwise.repository.packaging;

import com.fitwise.entity.User;
import com.fitwise.entity.packaging.PackageOfferMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PackageOfferMappingRepository extends JpaRepository<PackageOfferMapping,Long> {

    PackageOfferMapping findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailOfferCodeId(Long subscriptionPackageId, Long offerId);

    PackageOfferMapping findByOfferCodeDetailOfferCodeId(Long offerCodeDetailId);

    List<PackageOfferMapping> findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(Long subscriptionPackageId, boolean isOfferActive, String offerStatus);

    List<PackageOfferMapping> findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailIsNewUserAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferStatus(Long packageId, boolean isNewUser, boolean isInUse, String status);

    PackageOfferMapping findBySubscriptionPackageSubscriptionPackageIdAndOfferCodeDetailIsInUseAndOfferCodeDetailOfferNameAndOfferCodeDetailOwner(
            Long packageId, boolean isInUser, String offerReferenceName, User user
    );
}

