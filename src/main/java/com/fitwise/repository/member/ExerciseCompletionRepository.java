package com.fitwise.repository.member;

import com.fitwise.entity.ExerciseCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 15/05/20
 */
@Repository
public interface ExerciseCompletionRepository extends JpaRepository<ExerciseCompletion, Long> {

    /**
     *find user ExerciseCompletion row by programId, workoutScheduleId, circuitScheduleId, exerciseScheduleId
     * @param userId
     * @param programId
     * @param workoutScheduleId
     * @param circuitScheduleId
     * @param exerciseScheduleId
     * @return
     */
    ExerciseCompletion findTop1ByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleIdAndExerciseScheduleIdOrderByCompletedDateDesc(Long userId, Long programId, Long workoutScheduleId, Long circuitScheduleId, Long exerciseScheduleId);

    /**
     * @param circuitScheduleId
     * @return
     */
    List<ExerciseCompletion> findByCircuitScheduleId(Long circuitScheduleId);

    /**
     *
     * @param exerciseScheduleId
     * @return
     */
    List<ExerciseCompletion> findByExerciseScheduleId(Long exerciseScheduleId);

    /**
     * List of Exercise completed under by an user in a program
     * @param userId
     * @param programId
     * @return
     */
    List<ExerciseCompletion> findByMemberUserIdAndProgramProgramId(Long userId, Long programId);

    /**
     * @param userId
     * @return
     */
    List<ExerciseCompletion> findByMemberUserId(Long userId);

    List<ExerciseCompletion> findByWorkoutScheduleIdIn(List<Long> workoutScheduleIdList);

    /**
     * @param circuitScheduleIdList
     */
    List<ExerciseCompletion> findByCircuitScheduleIdIn(List<Long> circuitScheduleIdList);

    /**
     * @param exerciseScheduleIdList
     */
    List<ExerciseCompletion> findByExerciseScheduleIdIn(List<Long> exerciseScheduleIdList);

    List<ExerciseCompletion> findByMemberUserIdAndProgramProgramIdAndWorkoutScheduleIdAndCircuitScheduleId(Long userId, Long programId, Long workoutScheduleId, Long circuitScheduleId);


}
