package com.fitwise.view.instructor;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InstructorWorkoutResponseView {
    private long workoutId;
    private boolean isRestDay;
    private String workoutThumbnail;
    private String workoutTitle;
    private long workoutOrder;
    private int exercisesCount;
    private int workoutDuration;
    private boolean isTrail = false;
    private String day;
    private List<InstructorExerciseResponseView> exercises;
    private boolean isVideoProcessingPending;
}
