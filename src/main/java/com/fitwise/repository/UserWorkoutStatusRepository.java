package com.fitwise.repository;

import com.fitwise.entity.User;
import com.fitwise.entity.UserWorkoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface UserWorkoutStatusRepository extends JpaRepository<UserWorkoutStatus, Long> {

    @Query(value = "select uws from UserWorkoutStatus uws where uws.user =:user and uws.completionDate between :startDate and :endDate group by uws.workouts ")
    List<UserWorkoutStatus> getUniqueCompletedWorkouts(@Param("user") User user, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
