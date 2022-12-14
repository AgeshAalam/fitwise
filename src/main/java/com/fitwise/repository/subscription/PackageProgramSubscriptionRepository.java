package com.fitwise.repository.subscription;

import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.entity.subscription.PackageProgramSubscription;
import com.fitwise.entity.subscription.PackageSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 06/01/21
 */
@Repository
public interface PackageProgramSubscriptionRepository extends JpaRepository<PackageProgramSubscription, Long> {

    /**
     * @param packageSubscription
     * @param user
     * @param program
     * @return
     */
    PackageProgramSubscription findByPackageSubscriptionAndUserAndProgram(PackageSubscription packageSubscription, User user, Programs program);

    List<PackageProgramSubscription> findByUserAndProgram(User user, Programs program);

    PackageProgramSubscription findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(Long userId, Long programId);

    /**
     * @param userId
     * @param statusList
     * @param searchTitle
     * @return
     */
    List<PackageProgramSubscription> findByUserUserIdAndSubscriptionStatusSubscriptionStatusNameInAndProgramTitleIgnoreCaseContaining(Long userId, List<String> statusList, String searchTitle);

    /**
     * @param userId
     * @param statusList
     * @return
     */
    List<PackageProgramSubscription> findByUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(Long userId, List<String> statusList);

    /**
     * @param userId
     * @param programStatusList
     * @param statusList
     * @return
     */
    List<PackageProgramSubscription> findByUserUserIdAndProgramStatusInAndSubscriptionStatusSubscriptionStatusNameInOrderBySubscribedDateDesc(Long userId, List<String> programStatusList, List<String> statusList);
   
    PackageProgramSubscription findByPackageSubscriptionAndProgramProgramId(PackageSubscription packageSubscription,Long programId);
    
    /**
     * Delete by id.
     *
     * @param id
     */
    void deleteById(final Long Id);

}
