package com.fitwise.view.instructor;

import com.fitwise.view.circuit.CircuitScheduleResponseView;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InstructorWorkoutDetailResponseView {
    private long workoutId;
    private String workoutThumbnail;
    //private String workoutTitle;
    private long workoutDuration;
    private boolean isVideoProcessingPending;
    List<CircuitScheduleResponseView> circuitSchedules;
}
