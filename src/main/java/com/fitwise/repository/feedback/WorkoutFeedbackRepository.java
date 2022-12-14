package com.fitwise.repository.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fitwise.entity.WorkoutFeedback;

import java.util.Date;
import java.util.List;

@Repository
public interface WorkoutFeedbackRepository extends JpaRepository<WorkoutFeedback, Long>{

    /**
     *
     * @param workoutScheduleIds
     * @param FeedbackType
     * @return
     */
    int countByWorkoutScheduleWorkoutScheduleIdInAndFeedbackTypeFeedbackTypeAndCreatedDateBetween(List<Long> workoutScheduleIds, String FeedbackType,Date startDate, Date endDate);

    /**
     * Count of feedback type within given dates
     *
     * @param feedbackType
     * @param startDate
     * @param endDate
     * @return
     */
    int countByFeedbackTypeFeedbackTypeAndCreatedDateBetween(String feedbackType, Date startDate, Date endDate);

    /**
     * @param programId
     * @param workoutScheduleId
     * @param userId
     * @return
     */
    List<WorkoutFeedback> findByProgramProgramIdAndWorkoutScheduleWorkoutScheduleIdAndUserUserId(Long programId, Long workoutScheduleId, Long userId);

    /**
     * @param userId
     * @return
     */
    List<WorkoutFeedback> findByUserUserId(Long userId);

    /**
     * @param workoutScheduleIds
     * @return
     */
    List<WorkoutFeedback> findByWorkoutScheduleWorkoutScheduleIdIn(List<Long> workoutScheduleIds);


}
