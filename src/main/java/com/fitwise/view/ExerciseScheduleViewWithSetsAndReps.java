package com.fitwise.view;

import com.fitwise.view.circuit.ExerciseScheduleView;
import lombok.Data;

@Data
public class ExerciseScheduleViewWithSetsAndReps extends ExerciseScheduleView {

    private String playType;
    private int setsCount;
    private int repsCount;
}
