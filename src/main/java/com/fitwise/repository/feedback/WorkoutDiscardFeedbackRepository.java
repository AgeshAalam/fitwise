package com.fitwise.repository.feedback;

import com.fitwise.entity.WorkoutDiscardFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/*
 * Created by Vignesh G on 10/07/20
 */
@Repository
public interface WorkoutDiscardFeedbackRepository extends JpaRepository<WorkoutDiscardFeedback, Long> {

    /**
     * @param workoutScheduleIds
     * @param discardReason
     * @return
     */
    int countByWorkoutScheduleWorkoutScheduleIdInAndWorkoutDiscardFeedbackMappingDiscardWorkoutReasonDiscardReasonAndCreatedDateBetween(List<Long> workoutScheduleIds, String discardReason,Date startDate, Date endDate);

    /**
     * @param workoutScheduleIds
     * @param discardReason
     * @return
     */
    Page<WorkoutDiscardFeedback> findByWorkoutScheduleWorkoutScheduleIdInAndWorkoutDiscardFeedbackMappingDiscardWorkoutReasonDiscardReason(List<Long> workoutScheduleIds, String discardReason, Pageable pageable);

    /**
     * @param discardReason
     * @param startDate
     * @param endDate
     * @return
     */
    int countByWorkoutDiscardFeedbackMappingDiscardWorkoutReasonDiscardReasonAndCreatedDateBetween(String discardReason, Date startDate, Date endDate);

    /**
     * @param discardReason
     * @param startDate
     * @param endDate
     * @return
     */
    Page<WorkoutDiscardFeedback> findByWorkoutDiscardFeedbackMappingDiscardWorkoutReasonDiscardReasonAndCreatedDateBetween(String discardReason, Date startDate, Date endDate, Pageable pageable);

    /**
     * @param workoutScheduleIds
     * @return
     */
    List<WorkoutDiscardFeedback> findByWorkoutScheduleWorkoutScheduleIdIn(List<Long> workoutScheduleIds);

}
