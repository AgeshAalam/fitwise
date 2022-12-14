package com.fitwise.repository.subscription;

import com.fitwise.entity.User;
import com.fitwise.entity.subscription.PackageProgramSubscription;
import com.fitwise.entity.subscription.PackageSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 18/12/20
 */
@Repository
public interface PackageSubscriptionRepository extends JpaRepository<PackageSubscription, Long> , JpaSpecificationExecutor<PackageSubscription> {

    /**
     * @param userId
     * @param subscriptionPackageId
     * @return
     */
    PackageSubscription findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdOrderBySubscribedDateDesc(Long userId, Long subscriptionPackageId);

    /**
     * @param subscriptionPackageId
     * @return
     */
    List<PackageSubscription> findBySubscriptionPackageSubscriptionPackageId(Long subscriptionPackageId);

    /**
     * @param subscriptionPackageIdList
     * @return
     */
    List<PackageSubscription> findBySubscriptionPackageSubscriptionPackageIdIn(List<Long> subscriptionPackageIdList);

    List<PackageSubscription> findByUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(Long userId, List<String> statusList);

    Page<PackageSubscription> findByUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(Long userId, List<String> statusList, Pageable pageable);

    /**
     * Get Package subscriptions of isntructor's client
     * @param instructor
     * @param member
     * @param statusList
     * @return
     */
    List<PackageSubscription> findByUserAndSubscriptionPackageOwnerAndSubscriptionStatusSubscriptionStatusNameIn(User member, User instructor, List<String> statusList, Sort sort);

    List<PackageSubscription> findByUserUserIdAndSubscriptionStatusSubscriptionStatusNameInAndSubscriptionPackageTitleIgnoreCaseContaining(Long userId, List<String> statusList, String searchName);

    List<PackageSubscription> findByUserUserIdOrderBySubscribedDateDesc(Long userId);

    PackageSubscription findTop1ByUserUserIdAndSubscriptionPackageSubscriptionPackageIdInOrderBySubscribedDateDesc(Long userId, List<Long> subscriptionPackageIds);

    PackageSubscription findByPackageProgramSubscription(PackageProgramSubscription packageProgramSubscriptions);

    List<PackageSubscription> findBySubscriptionPackageOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(Long userId, List<String> statusList);


    /**
     * @param userId
     * @return
     */
    List<PackageSubscription> findBySubscriptionPackageOwnerUserId(Long userId);

    List<PackageSubscription> findBySubscriptionPackageOwnerUserIdAndUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(Long instructorId, Long memberId, List<String> statusList);

    List<PackageSubscription> findBySubscriptionPackageOwnerUserIdAndUserUserId(Long instructorId,Long memberId);



}
