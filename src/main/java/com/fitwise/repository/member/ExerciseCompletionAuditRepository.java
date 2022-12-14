package com.fitwise.repository.member;

import com.fitwise.entity.ExerciseCompletion;
import com.fitwise.entity.ExerciseCompletionAudit;
import com.fitwise.entity.ExerciseSchedulers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Created by Vignesh G on 01/07/20
 */
@Repository
public interface ExerciseCompletionAuditRepository extends JpaRepository<ExerciseCompletionAudit, Long> {

    /**
     * @param workoutScheduleIdList
     */
    void deleteByWorkoutScheduleIdIn(List<Long> workoutScheduleIdList);

    List<ExerciseCompletionAudit> findByWorkoutScheduleIdIn(List<Long> workoutScheduleIdList);

    /**
     * @param circuitScheduleIdList
     */
    List<ExerciseCompletionAudit> findByCircuitScheduleIdIn(List<Long> circuitScheduleIdList);

    /**
     * @param exerciseScheduleIdList
     */
    List<ExerciseCompletionAudit> findByExerciseScheduleIdIn(List<Long> exerciseScheduleIdList);

    /**
     * @param exerciseScheduleId
     * @return
     */
    List<ExerciseCompletionAudit> findByExerciseScheduleId(Long exerciseScheduleId);

}
