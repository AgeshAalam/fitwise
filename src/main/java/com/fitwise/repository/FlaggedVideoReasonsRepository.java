package com.fitwise.repository;

import com.fitwise.entity.FlaggedVideoReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * Created by Vignesh G on 26/03/20
 */

@Repository
public interface FlaggedVideoReasonsRepository extends JpaRepository<FlaggedVideoReason, Long> {

    /**
     * Get FlaggedVideoReason entity for a given feedbackReason
     *
     * @param feedbackReason
     * @return
     */
    FlaggedVideoReason findByFeedbackReason(String feedbackReason);

}
