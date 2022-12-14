package com.fitwise.repository.member;

import com.fitwise.entity.WorkoutCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 16/05/20
 */
@Repository
public interface WorkoutCompletionRepository extends JpaRepository<WorkoutCompletion, Long> {

    /**
     * Find User's workout completion entry for a program's workout schedule Id
     * @param userId
     * @param programId
     * @param workoutScheduleId
     * @return
     */
    WorkoutCompletion findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdOrderByCompletedDateDesc(Long userId, Long programId, Long workoutScheduleId);

    /**
     * No of workouts completed by user in a program
     * @param userId
     * @param programId
     * @return
     */
    List<WorkoutCompletion> findByMemberUserIdAndProgramProgramId(Long userId, Long programId);

    /**
     * @param userId
     * @param programId
     * @return
     */
    int countByMemberUserIdAndProgramProgramId(Long userId, Long programId);

    List<WorkoutCompletion> findByMemberUserId(Long userId);

    /**
     * @param workoutScheduleIdList
     */
    void deleteByWorkoutScheduleIdIn(List<Long> workoutScheduleIdList);

    List<WorkoutCompletion> findByWorkoutScheduleIdIn(List<Long> workoutScheduleIdList);

}
