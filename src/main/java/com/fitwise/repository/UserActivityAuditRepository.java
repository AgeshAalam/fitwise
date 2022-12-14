package com.fitwise.repository;

import com.fitwise.entity.User;
import com.fitwise.entity.UserActivityAudit;
import com.fitwise.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/*
 * Created by Vignesh G on 16/03/20
 */

@Repository
public interface UserActivityAuditRepository extends JpaRepository<UserActivityAudit, Long> {

    List<UserActivityAudit> findByUserAndUserRoleAndLastActiveTimeBetween(User user, UserRole userRole, Date startDate, Date endDate);

    List<UserActivityAudit> findByLastActiveTimeBetween(Date startDate, Date endDate);

    int countByUserRoleAndLastActiveTimeBetween(UserRole userRole,Date startDate, Date endDate);


}
