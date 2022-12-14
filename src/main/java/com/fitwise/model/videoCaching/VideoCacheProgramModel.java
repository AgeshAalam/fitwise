package com.fitwise.model.videoCaching;


import lombok.Data;

import java.util.List;

@Data
public class VideoCacheProgramModel {

    private Long programId;

    private List<VideoCacheWorkoutModel> workoutSchedules;

    private Float programRating;

    private boolean promoCompletionStatus;

    private Long promoCompletionDateInMillis;
}
