package com.fitwise.view.workout;

import lombok.Data;

/*
 * Created by Vignesh G on 26/04/20
 */
@Data
public class WorkoutLibraryView {

    private Long workoutId;
    private String thumbnailUrl;
    private String title;
    private long duration;
    private int circuitCount;
    private boolean isVideoProcessingPending;
    private String status;
    private int activeSubscriptionCount;
    private int associatedInProgressProgramCount;
    private boolean containsBlockedExercise;

}
