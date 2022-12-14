package com.fitwise.workout.model;

import com.fitwise.view.circuit.CircuitScheduleResponseView;
import lombok.Data;

import java.util.List;

@Data
public class WorkoutResponseView {

    private Long workoutId;
    private Long imageId;
    private String thumbnailUrl;
    private String title;
    private String description;
    private boolean flag;
    private Long instructorId;
    private long duration;
    private String status;
    private int activeSubscriptionCount;

    private int circuitCount;
    List<CircuitScheduleResponseView> circuitSchedules;
    private boolean isVideoProcessingPending;

    private int associatedInProgressProgramCount;

}
