package com.fitwise.repository.packaging;

import com.fitwise.entity.packaging.BlockedPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 29/01/21
 */
@Repository
public interface BlockedPackageRepository extends JpaRepository<BlockedPackage, Long> {

    /**
     * @param subscriptionPackageId
     * @param blockType
     * @return
     */
    BlockedPackage findBySubscriptionPackageSubscriptionPackageIdAndBlockType(Long subscriptionPackageId, String blockType);

    /**
     * @param subscriptionPackageId
     * @return
     */
    boolean existsBySubscriptionPackageSubscriptionPackageId(Long subscriptionPackageId);

    /**
     * @param subscriptionPackageId
     */
    void deleteBySubscriptionPackageSubscriptionPackageId(Long subscriptionPackageId);

    /**
     * @param subscriptionPackageIdList
     * @param blockType
     * @return
     */
    List<BlockedPackage> findBySubscriptionPackageSubscriptionPackageIdInAndBlockType(List<Long> subscriptionPackageIdList, String blockType);
}
