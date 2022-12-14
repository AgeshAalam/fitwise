package com.fitwise.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.fitwise.entity.*;
import com.fitwise.entity.payments.authNet.AuthNetPayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * The Interface ProgramRepository.
 */
@Repository
public interface ProgramRepository extends JpaRepository<Programs, Long>, JpaSpecificationExecutor<Programs> {

    /**
     * Find by program id.
     *
     * @param id the id
     * @return the programs
     */
    Programs findByProgramId(Long id);

   /**
    * Find by program id and owner user id.
    *
    * @param programId the program id
    * @param userId the user id
    * @return the list
    */
   Programs findByProgramIdAndOwnerUserId(final Long programId, final Long userId);

   /**
    * Find by owner user id.
    *
    * @param userId the user id
    * @return the list
    */
   List<Programs> findByOwnerUserId(final Long userId);

   /**
    * Find by owner user id.
    *
    * @param userId the user id
    * @param pageable the pageable
    * @return the list
    */
   Page<Programs> findByOwnerUserId(final Long userId, final Pageable pageable);

   /**
    * Find by owner user id and status.
    *
    * @param userId the user id
    * @param status the status
    * @param pageable the pageable
    * @return the list
    */
   Page<Programs> findByOwnerUserIdAndStatusOrderByModifiedDateDesc(final Long userId, final String status, final Pageable pageable);

    /**
     * Find by owner user id and status in.
     *
     * @param userId the user id
     * @param statusList the status list
     * @param pageable the pageable
     * @return the list
     */
    Page<Programs> findByOwnerUserIdAndStatusIn(final Long userId, final List<String> statusList, final Pageable pageable);

   /**
    * Find by program type program type id.
    *
    * @param expertlevel the expertlevel
    * @return the list
    */
    
    List<Programs> findByProgramExpertiseLevel(final ExpertiseLevels expertlevel);
    
   // List<Programs> findByProgramTypeName(final ProgramTypes programType);
    
    /**
    * Find by price.
    *
    * @param  programPrice
    * @return the list
    */
   List<Programs> findByProgramPrice(final Long programPrice);
    
    /**
     * Find by duration.
     *
     * @param duration the duration
     * @return the list
     */
    List<Programs> findByDuration(final Long duration);
    
   /**
    * Find by program type program type id.
    *
    * @param programTypeId the program type id
    * @return the list
    */
   List<Programs> findByProgramTypeProgramTypeId(final Long programTypeId);

    /**
     * Find by experience.
     *
     * @param instructorExperience the instructor experience
     * @return the list
     */
    List<Programs> findByInstructorYearOfExperience(final InstructorProgramExperience instructorExperience);

    /**
     * Find by title ignore case containing.
     *
     * @param title the title
     * @return the list
     */
    List<Programs> findByTitleIgnoreCaseContaining(final String title);
    
    /**
     * Find by program type.
     *
     * @param programType the program type
     * @return the list
     */
    List<Programs> findByProgramType(final ProgramTypes programType);
    
    /**
     * Find by duration.
     *
     * @param duration the duration
     * @return the list
     */
    List<Programs> findByDuration(final List<Long> duration);

   /**
    * Find by program type in and program expertise level in, in published state
    *
    * @param programTypes the program types
    * @param expertiseLevels the expertise levels
    * @param status
    * @return the page
    */
   List<Programs> findByProgramTypeInAndProgramExpertiseLevelInAndStatus(final List<ProgramTypes> programTypes, final List<ExpertiseLevels> expertiseLevels, String status);

    /**
     * @param programTypes
     * @param expertiseLevels
     * @param status
     * @param titleSearch
     * @return
     */
   List<Programs> findByProgramTypeInAndProgramExpertiseLevelInAndStatusAndTitleIgnoreCaseContaining(final List<ProgramTypes> programTypes, final List<ExpertiseLevels> expertiseLevels, String status, String titleSearch);


    /**
     * Find by program type.
     *
     * @param programType the program type
     * @param pageable the pageable
     * @return the page
     */
    Page<Programs> findByProgramType(final ProgramTypes programType, final Pageable pageable);

    /**
     * Find by program type id.
     *
     * @param programTypeId the program type
     * @param pageable the pageable
     * @return the programs
     */
    Page<Programs> findByProgramTypeProgramTypeIdAndStatus(final Long programTypeId, String status, final Pageable pageable);


    /**
     * Find by user id.
     *
     * @param userId the user id
     * @return count of user
     */
    long countByOwnerUserId(Long userId);

    /**
     * Find by program type program type id in and owner user id in.
     *
     * @param programTypeId the program type id
     * @param userId the user id
     * @return the list
     */
    List<Programs> findByProgramTypeProgramTypeIdInAndOwnerUserIdIn(long programTypeId, Long userId);

    /**
     * Find by program type program type id and title ignore case containing.
     *
     * @param programTypeId the program type id
     * @param searchName the title
     * @return the list
     */
    List<Programs> findByProgramTypeProgramTypeIdInAndStatusInAndTitleIgnoreCaseContaining(List<Long> programTypeId , List<String> statusList, String searchName);


    /**
     * find by promotion id
     * @param promotionId
     * @return
     */
    List<Programs> findByPromotionPromotionId(long promotionId);

    /**
     * find by image id
     * @param thumbnailId
     * @return
     */
    List<Programs> findByImageImageId(long thumbnailId);

    int countByProgramTypeAndStatusAndCreatedDateBetween(ProgramTypes programType, String status, Date firstWeekStartDate, Date firstWeekEndDate);

    int countByProgramTypeAndCreatedDateBetween(ProgramTypes programType,  Date firstWeekStartDate, Date firstWeekEndDate);


    @Query(value = "SELECT count(*) FROM programs p where  year(p.created_date) =:year " +
            "AND (month(p.created_date) =:month) and p.program_type_id=:programTypeId", nativeQuery = true)
    long countForMonth(long year , int month , long programTypeId);

    @Query(value = "SELECT * FROM programs p " +
            "inner join program_types pt on p.program_type_id = pt.program_type_id " +
            "inner join expertise_levels el on p.program_expertise_level_id = el.expertise_level_id " +
            "inner join user u on p.user_id = u.user_id " +
            "inner join user_profile up on up.user_id = u.user_id " +
            "inner join user_role_mapping ur on ur.user_id = u.user_id " +
            "inner join role r on r.role_id = ur.role_id where r.name = 'instructor' and p.status ='publish' " +
            "and (concat(REPLACE(up.first_name,' ', '') , REPLACE(up.last_name,' ', '')) like %:userName% or " +
            " REPLACE(p.title,' ', '') like %:searchName% or REPLACE(pt.program_type_name,' ', '') like %:searchName% or REPLACE(el.expertise_level,' ', '') like %:searchName%)", nativeQuery = true)
    List<Programs> getSearchedPrograms(String userName, String searchName);

    List<Programs> findByStatusAndModifiedDateAfterOrderByProgramIdAsc(String publish, Date date);
    
    List<Programs> findByStatus(String publish);

    Page<Programs> findByStatus(String publish, Pageable pageable);

    /**
     * @param publish
     * @param titleSearch
     * @param pageable
     * @return
     */
    Page<Programs> findByStatusAndTitleIgnoreCaseContaining(String publish, String titleSearch, Pageable pageable);

    /**
     * Get page of programs by status and duration list
     * @param publish
     * @param durationIdList
     * @param pageable
     * @return
     */
    Page<Programs> findByStatusAndDurationDurationIdIn(String publish, List<Long> durationIdList, Pageable pageable);

    /**
     * @param publish
     * @param durationIdList
     * @param titleSearch
     * @param pageable
     * @return
     */
    Page<Programs> findByStatusAndDurationDurationIdInAndTitleIgnoreCaseContaining(String publish, List<Long> durationIdList, String titleSearch, Pageable pageable);

    List<Programs> findByOwnerUserIdAndStatus(final Long userId,String status);

    /**
     * @param userId
     * @param status
     * @param pageable
     * @return
     */
    Page<Programs> findByOwnerUserIdAndStatus(final Long userId,String status, Pageable pageable);

    /**
     * Get the program count for given user and status
     * @param userId
     * @param status
     * @return
     */
    long countByOwnerUserIdAndStatus(final Long userId,String status);

    List<Programs> findByOwnerUserIdAndStatusIn(final Long userId, final List<String> statusList);

    /**
     * Get the program count for given user and the status list
     * @param userId
     * @param statusList
     * @return
     */
    long countByOwnerUserIdAndStatusIn(final Long userId, final List<String> statusList);

    /**
     * Find program by owner and title ignore case
     *
     * @param userId
     * @param title
     * @return Program by owner and title
     */
    Programs findByOwnerUserIdAndTitleIgnoreCase(long userId, String title);

    long countByProgramTypeProgramTypeIdAndStatusIn(long programTypeId, List<String> status);

 /**
  * @param user
  * @param programTypeId
  * @param status
  * @return
  */
    long countByOwnerAndProgramTypeProgramTypeIdAndStatusIn(User user, long programTypeId, List<String> status);

    /**
     * @param status
     * @return
     */
    long countByStatusIn(List<String> status);

 /**
  * @param user
  * @param status
  * @return
  */
    long countByOwnerAndStatusIn(User user, List<String> status);

    List<Programs> findByProgramTypeProgramTypeIdInAndStatusInAndOwnerUserIdIn(List<Long> programTypeId, List<String> statusList, Long userId);

    List<Programs> findByProgramTypeProgramTypeIdInAndStatusIn(List<Long> programTypeId, List<String> statusList);

    /**
     * Count of programs in a particular status until a given time.
     * @param status
     * @param lastMonthLastDate
     * @return
     */
    int countByStatusAndModifiedDateLessThan(String status, Date lastMonthLastDate);

    /**
     * @param programTypeId
     * @param status
     * @param searchName
     * @param pageable
     * @return
     */
    Page<Programs> findByProgramTypeProgramTypeIdAndStatusAndTitleIgnoreCaseContaining(long programTypeId , String status, String searchName, Pageable pageable);

    /**
     * @param programTypeId
     * @param durationIdList
     * @param expertiseLevelIdList
     * @param status
     * @param pageable
     * @return
     */
    Page<Programs> findByProgramTypeProgramTypeIdAndDurationDurationIdInAndProgramExpertiseLevelExpertiseLevelIdInAndStatus(Long programTypeId, List<Long> durationIdList, List<Long> expertiseLevelIdList, String status, Pageable pageable);

    /**
     * @param programTypeId
     * @param durationIdList
     * @param expertiseLevelIdList
     * @param status
     * @param titleSearch
     * @param pageable
     * @return
     */
    Page<Programs> findByProgramTypeProgramTypeIdAndDurationDurationIdInAndProgramExpertiseLevelExpertiseLevelIdInAndStatusAndTitleIgnoreCaseContaining(Long programTypeId, List<Long> durationIdList, List<Long> expertiseLevelIdList, String status, String titleSearch, Pageable pageable);

    /**
     * @param programTypeIdList
     * @param durationIdList
     * @param expertiseLevelIdList
     * @param status
     * @param pageable
     * @return
     */
    Page<Programs> findByProgramTypeProgramTypeIdInAndDurationDurationIdInAndProgramExpertiseLevelExpertiseLevelIdInAndStatus(List<Long> programTypeIdList, List<Long> durationIdList, List<Long> expertiseLevelIdList, String status, Pageable pageable);

    /**
     * @param programTypeIdList
     * @param durationIdList
     * @param expertiseLevelIdList
     * @param status
     * @param titleSearch
     * @param pageable
     * @return
     */
    Page<Programs> findByProgramTypeProgramTypeIdInAndDurationDurationIdInAndProgramExpertiseLevelExpertiseLevelIdInAndStatusAndTitleIgnoreCaseContaining(List<Long> programTypeIdList, List<Long> durationIdList, List<Long> expertiseLevelIdList, String status, String titleSearch, Pageable pageable);

    Page<Programs> findByOwnerUserIdAndStatusInAndTitleIgnoreCaseContaining(long userId, List<String> statusList,String titleSearchName, Pageable pageable);

    Page<Programs> findByOwnerUserIdAndTitleIgnoreCaseContaining(long userId,String titleSearchName, Pageable pageable);
 /**
  * Getting latest program of instructor
  * @param userId
  * @return
  */
    Programs findTop1ByOwnerUserIdOrderByModifiedDateDesc(Long userId);

 /**
  * @param userId
  * @param programTypeId
  * @param status
  * @param pageable
  * @return
  */
 Page<Programs> findByOwnerUserIdAndProgramTypeProgramTypeIdInAndStatus(Long userId, List<Long> programTypeId, String status, Pageable pageable);

    /**
     * @param userId
     * @param programTypeId
     * @param status
     * @param searchName
     * @param pageable
     * @return
     */
    Page<Programs> findByOwnerUserIdAndProgramTypeProgramTypeIdInAndStatusAndTitleIgnoreCaseContaining(Long userId, List<Long> programTypeId, String status, String searchName, Pageable pageable);

    long countByOwnerAndProgramTypeProgramTypeIdAndStatusIn(User user, Long programTypeId, List<String> statusList);
    
	@Query(value = "SELECT * FROM programs p where  p.user_id =:userId AND p.status ='publish' AND p.program_sub_type_id IS NOT NULL GROUP by p.program_sub_type_id", nativeQuery = true)
	List<Programs> findByOwnerAndProgramSubTypeIsNotNull(long userId);
}