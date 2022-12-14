package com.fitwise.repository.feedback;

import com.fitwise.entity.DiscardWorkoutReasons;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscardWorkoutReasonsRepository extends JpaRepository<DiscardWorkoutReasons, Long> {

    DiscardWorkoutReasons findByDiscardFeedbackId(long id);

}
