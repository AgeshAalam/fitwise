package com.fitwise.repository;

import com.fitwise.entity.UserActivityTracker;
import com.fitwise.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActivityTrackerRepository extends JpaRepository<UserActivityTracker, Long> {
    UserActivityTracker findByUserAndUserRole(long userId, UserRole userRole);
}
