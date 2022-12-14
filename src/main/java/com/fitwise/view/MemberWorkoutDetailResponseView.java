package com.fitwise.view;

import com.fitwise.view.circuit.CircuitScheduleResponseView;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemberWorkoutDetailResponseView {

    private String workoutThumbnail;
    private String workoutName;
    private String displayTitle;
    private Long workoutScheduleId;
    private long workoutDuration;
    private int circuitCount;
    private List<CircuitScheduleResponseView> circuitSchedules;
    private boolean isVideoProcessingPending;

}
