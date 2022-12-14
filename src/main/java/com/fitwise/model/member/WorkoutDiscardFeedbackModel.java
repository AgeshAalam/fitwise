package com.fitwise.model.member;

import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 10/07/20
 */
@Data
public class WorkoutDiscardFeedbackModel {

    private Long programId;

    private Long workoutScheduleId;

    private List<DiscardReasonModel> discardReasons;

}
