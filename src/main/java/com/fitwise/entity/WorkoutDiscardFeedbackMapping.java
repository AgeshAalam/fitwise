package com.fitwise.entity;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/*
 * Created by Vignesh G on 09/07/20
 */
@Entity
@Data
public class WorkoutDiscardFeedbackMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long mappingId;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.DETACH})
    private WorkoutDiscardFeedback workoutDiscardFeedback;

    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "discard_feedback_id")
    private DiscardWorkoutReasons discardWorkoutReason;

    private String customReason;

}
