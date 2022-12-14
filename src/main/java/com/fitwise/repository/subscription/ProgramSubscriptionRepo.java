package com.fitwise.repository.subscription;

import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.ProgramTypes;
import com.fitwise.entity.Programs;
import com.fitwise.entity.User;
import com.fitwise.response.HighestSubscribedPrograms;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ProgramSubscriptionRepo extends JpaRepository<ProgramSubscription, Long>, JpaSpecificationExecutor<ProgramSubscription> {
    List<ProgramSubscription> findByUserUserIdOrderBySubscribedDateDesc(Long userId);

    /**
     * @param userId
     * @return
     */
    List<ProgramSubscription> findByUserUserId(Long userId);

    ProgramSubscription findTop1ByUserUserIdAndProgramProgramIdOrderBySubscribedDateDesc(Long userId, Long programId);

    List<ProgramSubscription> findByProgramOwnerUserId(Long instructorId);

    List<ProgramSubscription> findByProgramProgramId(Long programId);

    /**
     * @param programIdList
     * @return
     */
    List<ProgramSubscription> findByProgramProgramIdIn(List<Long> programIdList);

    List<ProgramSubscription> findByProgramProgramTypeProgramTypeId(Long programTypeId);

    List<Programs> findByUserAndProgramProgramType(User user, ProgramTypes programType);

    @Query(" select new com.fitwise.response.HighestSubscribedPrograms(count(*) as countOfSubscription , ps.program.programId )  " +
            " from ProgramSubscription ps where ps.program.status = 'Publish' group by ps.program.programId order by countOfSubscription desc ")
    List<HighestSubscribedPrograms> getHighestSubscribedPrograms();

    boolean existsByProgram(Programs program);

    List<ProgramSubscription> findByUserUserIdAndProgramStatusInAndSubscriptionStatusSubscriptionStatusNameInOrderBySubscribedDateDesc(Long userId, List<String> programStatusList, List<String> statusList);

    /**
     * @param userId
     * @param statusList
     * @param pageable
     * @return
     */
    Page<ProgramSubscription> findByUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(Long userId, List<String> statusList, Pageable pageable);

    /**
     * Search Get program subscriptions by member, subscription status,and program title search text
     * @param userId
     * @param statusList
     * @param searchTitle
     * @return
     */
    List<ProgramSubscription> findByUserUserIdAndSubscriptionStatusSubscriptionStatusNameInAndProgramTitleIgnoreCaseContaining(Long userId, List<String> statusList, String searchTitle);

    /**
     * Program subscriptions by instructor and status
     * @param instructorId
     * @param statusList
     * @return
     */
    List<ProgramSubscription> findByProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(Long instructorId, List<String> statusList);

    /**
     * @param instructorId
     * @param statusList
     * @param searchEmail
     * @return
     */
    List<ProgramSubscription> findByProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameInAndUserEmailIgnoreCaseContaining(Long instructorId, List<String> statusList, String searchEmail);

    /**
     * Program subscriptions by instructor and member
     * @param instructorId
     * @param userId
     * @return
     */
    List<ProgramSubscription> findByProgramOwnerUserIdAndUserUserId(Long instructorId, Long userId);

    /**
     * @param instructorId
     * @param userId
     * @return
     */
    boolean existsByProgramOwnerUserIdAndUserUserId(Long instructorId, Long userId);

    /**
     * Program subscriptions by instructor and member in a list of status
     * @param instructorId
     * @param userId
     * @param statusList
     * @return
     */
    List<ProgramSubscription> findByProgramOwnerUserIdAndUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(Long instructorId, Long userId, List<String> statusList);

    List<ProgramSubscription> findByUserUserIdAndSubscriptionStatusSubscriptionStatusNameIn(Long userId, List<String> statusList);

    /**
     * @param programId
     * @return
     */
    int countByProgramProgramId(Long programId);

    /**
     *
     * @param programId
     * @return
     */
    int countByProgramProgramIdAndModifiedDateGreaterThan(Long programId, Date fromDate);

}


