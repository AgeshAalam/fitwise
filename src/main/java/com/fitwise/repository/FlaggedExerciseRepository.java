package com.fitwise.repository;

import com.fitwise.entity.FlaggedExercise;
import com.fitwise.entity.FlaggedVideoReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/*
 * Created by Vignesh G on 03/07/20
 */
@Repository
public interface FlaggedExerciseRepository extends JpaRepository<FlaggedExercise, Long> {

    /**
     * Get row for user, exercise and reason
     * @param exerciseId
     * @param userId
     * @param reasonId
     * @return
     */
    FlaggedExercise findByExerciseExerciseIdAndUserUserIdAndFlaggedVideoReasonFeedbackId(Long exerciseId, Long userId, Long reasonId);

    /**
     * @param exerciseId
     * @return
     */
    List<FlaggedExercise> findByExerciseExerciseId(Long exerciseId);

    /**
     *
     * @param flaggedVideoReason
     * @param startDate
     * @param endDate
     * @return
     */
    int countByFlaggedVideoReasonAndCreatedDateBetween(FlaggedVideoReason flaggedVideoReason, Date startDate, Date endDate);

    /**
     * @param startDate
     * @param endDate
     * @return
     */
    int countByCreatedDateBetween(Date startDate, Date endDate);

}
