package com.fitwise.repository.member;

import com.fitwise.entity.WorkoutCompletionAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 01/07/20
 */
@Repository
public interface WorkoutCompletionAuditRepository extends JpaRepository<WorkoutCompletionAudit, Long> {

    /**
     * @param workoutScheduleIdList
     */
    void deleteByWorkoutScheduleIdIn(List<Long> workoutScheduleIdList);

    List<WorkoutCompletionAudit> findByWorkoutScheduleIdIn(List<Long> workoutScheduleIdList);

}
