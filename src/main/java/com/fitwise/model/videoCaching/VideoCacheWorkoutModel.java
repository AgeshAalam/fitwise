package com.fitwise.model.videoCaching;


import com.fitwise.model.member.DiscardReasonModel;
import lombok.Data;

import java.util.List;

@Data
public class VideoCacheWorkoutModel {

    private Long workoutScheduleId;

    List<DiscardReasonModel> discardReasons;

    private List<VideoCacheExerciseModel> videoCacheExerciseModels;

    private Long workoutCompletionDateInMillis;

    private Long workoutFeedbackTypeId;
}
