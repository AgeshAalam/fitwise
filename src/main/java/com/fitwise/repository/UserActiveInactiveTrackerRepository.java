package com.fitwise.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.UserActiveInactiveTracker;

/*
 * Created by Vignesh G on 23/03/20
 */
@Repository
public interface UserActiveInactiveTrackerRepository extends JpaRepository<UserActiveInactiveTracker, Long> {

    /**
     * Get row by user and role. If this query returns multiple rows, its an issue. Table should have only one row for user-role combo.
     *
     * @param userId
     * @param roleId
     * @return
     */
    List<UserActiveInactiveTracker> findByUserUserIdAndUserRoleRoleIdOrderByIdDesc(Long userId, Long roleId);

    /**
     * @param userId
     * @param roleId
     * @return
     */
    List<UserActiveInactiveTracker> findByUserUserIdAndUserRoleRoleId(Long userId, Long roleId);

    /**
     * Get users by status
     *
     * @param isActive
     * @return
     */
    List<UserActiveInactiveTracker> findByIsActiveAndModifiedDateLessThan(boolean isActive, Date lastModifiedDate);

    /**
     * Get users by role
     * @param userRole
     * @param sort
     * @return
     */
    List<UserActiveInactiveTracker> findByUserRoleName(String userRole, Sort sort);
    
    /**
     * Find top by user user id and user role role id order by id desc.
     *
     * @param userId the user id
     * @param roleId the role id
     * @return the user active inactive tracker
     */
    UserActiveInactiveTracker findTopByUserUserIdAndUserRoleRoleIdOrderByIdDesc(Long userId, Long roleId);


}
