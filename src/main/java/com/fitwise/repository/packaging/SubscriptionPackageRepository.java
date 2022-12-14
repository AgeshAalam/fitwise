package com.fitwise.repository.packaging;

import com.fitwise.entity.packaging.SubscriptionPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 23/09/20
 */
@Repository
public interface SubscriptionPackageRepository extends JpaRepository<SubscriptionPackage, Long>, JpaSpecificationExecutor<SubscriptionPackage> {

    /**
     * @param subscriptionPackageId
     * @param userId
     * @return
     */
    SubscriptionPackage findBySubscriptionPackageIdAndOwnerUserId(Long subscriptionPackageId, Long userId);

    /**
     * @param subscriptionPackageId
     * @return
     */
    SubscriptionPackage findBySubscriptionPackageId(Long subscriptionPackageId);

    /**
     * @param userId
     * @param title
     * @return
     */
    SubscriptionPackage findByOwnerUserIdAndTitleIgnoreCase(Long userId, String title);

    /**
     * @param userId
     * @param statusList
     * @param titleSearchName
     * @param pageable
     * @return
     */
    Page<SubscriptionPackage> findByOwnerUserIdAndStatusInAndTitleIgnoreCaseContaining(Long userId, List<String> statusList, String titleSearchName, Pageable pageable);

    /**
     * @param userId
     * @param statusList
     * @param pageable
     * @return
     */
    Page<SubscriptionPackage> findByOwnerUserIdAndStatusIn(Long userId, List<String> statusList, Pageable pageable);

    Page<SubscriptionPackage> findByOwnerUserIdAndStatus(Long userId, String status, Pageable pageable);

    /**
     * @param userId
     * @param status
     * @return
     */
    List<SubscriptionPackage> findByOwnerUserIdAndStatus(Long userId, String status);

    long countByOwnerUserIdAndStatus(Long userId, String status);

    List<SubscriptionPackage> findByStatus(String status);

    List<SubscriptionPackage> findByStatusInAndTitleIgnoreCaseContaining(List<String> statusList, String search);

    List<SubscriptionPackage> findByStatusIn(List<String> statusList);

    Page<SubscriptionPackage> findByOwnerUserIdAndStatusAndTitleIgnoreCaseContaining(Long userId, String status, String titleSearchName, Pageable pageable);

    List<SubscriptionPackage> findByPromotionPromotionId (Long promotionId);



}

