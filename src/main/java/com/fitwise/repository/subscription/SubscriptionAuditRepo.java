package com.fitwise.repository.subscription;

import com.fitwise.entity.ExpertiseLevels;
import com.fitwise.entity.subscription.ProgramSubscription;
import com.fitwise.entity.Programs;
import com.fitwise.entity.subscription.SubscriptionAudit;
import com.fitwise.entity.*;
import com.fitwise.entity.subscription.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SubscriptionAuditRepo extends JpaRepository<SubscriptionAudit, Long>, JpaSpecificationExecutor<SubscriptionAudit> {
    List<SubscriptionAudit> findAll();

    /**
     * Returns the subscriptions by a user
     * The Subscription must be in Paid state not in Trail state
     *
     * @param userId
     * @param subscriptionStatusId
     * @param subscriptionTypeName
     * @return
     */
    List<SubscriptionAudit> findBySubscriptionTypeNameAndUserUserIdAndSubscriptionStatusSubscriptionStatusId(String subscriptionTypeName, Long userId, Long subscriptionStatusId);


    /**
     * Returns count of subscriptions for a given'SubscriptionType', in a given 'SubscriptionStatus', for a 'programType', subscribed between two given dates
     *
     * @param SubscriptionTypeName
     * @param statusList
     * @param programTypeId
     * @param SubscriptionDateStart
     * @param SubscriptionDateEnd
     * @return
     */
    int countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramTypeProgramTypeIdAndSubscriptionDateBetween(String SubscriptionTypeName, List<String> statusList, Long programTypeId, Date SubscriptionDateStart, Date SubscriptionDateEnd);

    /**
     * Get count of program subscriptions for give instructorId and programExpertiseLevelId between 2 dates
     *
     * @param instructorId
     * @param programExpertiseLevel
     * @param subscriptionDateStart
     * @param subscriptionDateEnd
     * @return
     */
    int countBySubscriptionTypeNameIgnoreCaseAndProgramSubscriptionProgramOwnerUserIdAndProgramSubscriptionProgramProgramExpertiseLevelAndSubscriptionDateBetween(String SubscriptionTypeName, Long instructorId, ExpertiseLevels programExpertiseLevel, Date subscriptionDateStart, Date subscriptionDateEnd);

    /**
     * Get SubscriptionAudit list for Program subscriptions for give instructorId between 2 dates
     *
     * @param instructorId
     * @param subscriptionDateStart
     * @param subscriptionDateEnd
     * @return
     */
    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndProgramSubscriptionProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameInAndSubscriptionDateBetween(String SubscriptionTypeName, Long instructorId,List<String> statusList, Date subscriptionDateStart, Date subscriptionDateEnd);

    /**
     * Get Program subscription count for a programtype in a given date period
     *
     * @param SubscriptionTypeName
     * @param programTypeId
     * @param statusList
     * @param subscriptionDateStart
     * @param subscriptionDateEnd
     * @return
     */
    int countBySubscriptionTypeNameIgnoreCaseAndProgramSubscriptionProgramProgramTypeProgramTypeIdAndSubscriptionStatusSubscriptionStatusNameInAndSubscriptionDateBetween(String SubscriptionTypeName, Long programTypeId, List<String> statusList, Date subscriptionDateStart, Date subscriptionDateEnd);

    /**
     * Get Program subscription count for a duration in a given date period
     *
     * @param SubscriptionTypeName
     * @param durationId
     * @param statusList
     * @param subscriptionDateStart
     * @param subscriptionDateEnd
     * @return
     */
    int countBySubscriptionTypeNameIgnoreCaseAndProgramSubscriptionProgramDurationDurationIdAndSubscriptionStatusSubscriptionStatusNameInAndSubscriptionDateBetween(String SubscriptionTypeName, Long durationId, List<String> statusList, Date subscriptionDateStart, Date subscriptionDateEnd);


    /**
     * @param user
     * @param programId
     * @param statusList
     * @param createdDate
     * @param subscriptionTypeName
     * @return
     */
    int countByUserUserIdAndSubscriptionTypeNameAndProgramSubscriptionProgramProgramIdAndSubscriptionStatusSubscriptionStatusNameInAndCreatedDateLessThanEqual(long user, String subscriptionTypeName, Long programId, List<String> statusList, Date createdDate);

    /**
     * @param user
     * @param SubscriptionPackageId
     * @param statusList
     * @param createdDate
     * @return
     */
    int countBySubscriptionTypeNameAndUserUserIdAndPackageSubscriptionSubscriptionPackageSubscriptionPackageIdAndSubscriptionStatusSubscriptionStatusNameInAndCreatedDateLessThanEqual(String subscriptionTypeName, long user, Long SubscriptionPackageId, List<String> statusList, Date createdDate);


    /**
     * Finds the latest record from the table
     * @param user
     * @param programId
     * @param subscriptionTypename
     * @return
     */
    public SubscriptionAudit findTop1ByUserUserIdAndSubscriptionTypeNameAndProgramSubscriptionProgramProgramIdOrderByCreatedDateDesc(long user, String subscriptionTypename, Long programId);

    /**
     * @param userId
     * @param subscriptionPackageId
     * @return
     */
    public SubscriptionAudit findTop1BySubscriptionTypeNameAndUserUserIdAndPackageSubscriptionSubscriptionPackageSubscriptionPackageIdOrderByCreatedDateDesc(String subscriptionTypeName, long userId, Long subscriptionPackageId);


    List<SubscriptionAudit> findBySubscriptionTypeNameAndProgramSubscriptionProgramSubscriptionIdOrderBySubscriptionDateDesc(String subscriptionTypeName, Long programSubscriptionId);
    
    List<SubscriptionAudit> findBySubscriptionTypeNameAndTierSubscriptionTierSubscriptionIdOrderBySubscriptionDateDesc(String subscriptionTypeName, Long tierSubscriptionId);

    /**
     * @param packageSubscriptionId
     * @param subscriptionTypeName
     * @return
     */
    List<SubscriptionAudit> findBySubscriptionTypeNameAndPackageSubscriptionIdOrderBySubscriptionDateDesc(String subscriptionTypeName, Long packageSubscriptionId);

    /**
     * Get program subscription count based on start Date, end Date, status, programId and userId.
     *
     * @param startDate
     * @param endDate
     * @param status
     * @param programId
     * @param statusList
     * @param subscriptionTypeName
     * @return
     */

    int countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndProgramSubscriptionProgramProgramIdAndSubscriptionStatusSubscriptionStatusNameIn(String subscriptionTypeName, Date startDate, Date endDate, String status, Long programId, List<String> statusList);

    /**
     * Get program subscription count based on start Date, end Date and status.
     *
     * @param startDate
     * @param endDate
     * @param status
     * @param subscriptionTypeName
     * @return
     */
    int countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndSubscriptionStatusSubscriptionStatusNameIn(String subscriptionTypeName, Date startDate, Date endDate, String status, List<String> subscriptionStatusList);

    /**
     * Get program subscription count based on start Date, end Date, status and userId
     *
     * @param startDate
     * @param endDate
     * @param status
     * @param userId
     * @param subscriptionTypeName
     * @return
     */
    int countBySubscriptionTypeNameAndSubscriptionDateGreaterThanEqualAndSubscriptionDateLessThanEqualAndRenewalStatusAndProgramSubscriptionProgramOwnerUserIdAndSubscriptionStatusSubscriptionStatusNameIn(String subscriptionTypeName, Date startDate, Date endDate, String status, Long userId, List<String> statusList);

    /**
     * Get count of program subscriptions for given subscription type and status and programExpertiseLevelId between 2 dates
     *
     * @param SubscriptionTypeName
     * @param subscriptionStatusName
     * @param programExpertiseLevel
     * @param subscriptionDateStart
     * @param subscriptionDateEnd
     * @return
     */
    int countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramExpertiseLevelAndSubscriptionDateBetween(String SubscriptionTypeName, List<String> statusList, ExpertiseLevels programExpertiseLevel, Date subscriptionDateStart, Date subscriptionDateEnd);

    /**
     * Returns count of subscriptions for a given program owner , for a given'SubscriptionType', in a given 'SubscriptionStatus', for a 'platformType', subscribed between two given dates
     *
     * @param SubscriptionTypeName
     * @param statusList
     * @param instructorId
     * @param platformTypeId
     * @param SubscriptionDateStart
     * @param SubscriptionDateEnd
     * @return
     */
    int countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramOwnerUserIdAndSubscribedViaPlatformPlatformTypeIdAndSubscriptionDateBetween(String SubscriptionTypeName, List<String> statusList, Long instructorId, Long platformTypeId, Date SubscriptionDateStart, Date SubscriptionDateEnd);

    /**
     * Returns count of subscriptions for a given program id , for a given'SubscriptionType', in a given 'SubscriptionStatus', for a 'platformType', subscribed between two given dates
     *
     * @param SubscriptionTypeName
     * @param statusList
     * @param programId
     * @param platformTypeId
     * @param SubscriptionDateStart
     * @param SubscriptionDateEnd
     * @return
     */
    int countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramIdAndSubscribedViaPlatformPlatformTypeIdAndSubscriptionDateBetween(String SubscriptionTypeName, List<String> statusList, Long programId, Long platformTypeId, Date SubscriptionDateStart, Date SubscriptionDateEnd);

    /**
     * Returns count of subscriptions for a given'SubscriptionType', in a given 'SubscriptionStatus', for a 'platformType', subscribed between two given dates
     *
     * @param keyProgram
     * @param statusList
     * @param platformTypeId
     * @param startDate
     * @param endDate
     * @return
     */
    int countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndSubscribedViaPlatformPlatformTypeIdAndSubscriptionDateBetween(String keyProgram, List<String> statusList, Long platformTypeId, Date startDate, Date endDate);

    /**
     *History of paid subscriptions of instructor
     * @param SubscriptionTypeName
     * @param statusList
     * @param instructorId
     * @return
     */
    int countBySubscriptionTypeNameAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramOwnerUserId(String SubscriptionTypeName, List<String> statusList, Long instructorId);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndSubscribedViaPlatformPlatformTypeIdAndSubscriptionDateBetween(String keyProgram, List<String> statusList, Long platformTypeId, Date startDate, Date endDate);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndSubscriptionDateBetween(String keyProgram, List<String> statusList,  Date startDate, Date endDate);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscribedViaPlatformPlatformTypeIdAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramOwnerUserIdAndSubscriptionDateBetween(String keyProgram, long platformTypeId, List<String> statusList, long userId,  Date startDate, Date endDate);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndRenewalStatusIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramOwnerUserIdAndSubscriptionDateBetween(String keyProgram, String renewalStatus, List<String> statusList, long userId, Date startDate, Date endDate);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndRenewalStatusIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramIdAndSubscriptionDateBetween(String keyProgram, String renewalStatus, List<String> statusList, long programId, Date startDate, Date endDate);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramOwnerUserId(String keyProgram, List<String> statusList, long userId);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramOwnerUserIdAndSubscriptionDateBetween(String keyProgram, List<String> statusList, long userId, Date startDate, Date endDate);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramIdAndSubscriptionDateBetween(String keyProgram, List<String> statusList, long programId, Date startDate, Date endDate);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndUserUserId(String keyProgram, List<String> statusList, long userId);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramTypeProgramTypeIdAndSubscriptionDateBetween(String keyProgram, List<String> statusList, long programTypeId, Date startDate, Date endDate);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramId(String keyProgram, List<String> statusList, long programId);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscribedViaPlatformPlatformTypeIdAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramIdAndSubscriptionDateBetween(String keyProgram, long platformTypeId, List<String> statusList, long programId,  Date startDate, Date endDate);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramId(String keyProgram, String subscriptionStatus, long programId);

    SubscriptionAudit findBySubscriptionTypeNameAndProgramSubscriptionAndSubscriptionStatusSubscriptionStatusNameInAndRenewalStatus(String subscriptionTypeName, ProgramSubscription programSubscription, List<String> subscriptionStatu, String renewalStatus);

    /**
     * Paid subscription history of a program
     * @param SubscriptionTypeName
     * @param statusList
     * @param programId
     * @return
     */
    int countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndProgramSubscriptionProgramProgramId(String SubscriptionTypeName, List<String> statusList, Long programId);

    int countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndRenewalStatusAndSubscriptionDateBetween(String subscriptionTypeName, List<String> statusList, String renewalStatus,  Date startDate, Date endDate);

    int countBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameAndSubscriptionDateBetween(String subscriptionTypeName, String status,  Date startDate, Date endDate);

    List<SubscriptionAudit> findBySubscriptionTypeNameIgnoreCaseAndSubscriptionStatusSubscriptionStatusNameInAndPackageSubscriptionSubscriptionPackageOwnerUserId(String keyProgram, List<String> statusList, long userId);



}
