package com.fitwise.view.circuit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitwise.view.AudioResponseView;
import com.fitwise.view.ExerciseScheduleViewWithSetsAndReps;
import lombok.Data;

import java.util.List;

@Data
public class CircuitResponseViewWithSetsAndReps {

    private Long circuitId;
    private String title;
    private Long instructorId;
    private long duration;
    private int exerciseCount;
    private String status;

    List<ExerciseScheduleViewWithSetsAndReps> exerciseSchedules;

    private boolean isAudio;

    @JsonProperty("audioResponseViews")
    private List<AudioResponseView> audioResponseViews;
}
