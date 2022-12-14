package com.fitwise.workout.model;

import lombok.Data;

@Data
public class ExerciseMappingModelWithSetsAndReps extends ExerciseMappingModel  {

    private String playType;
    private int setsCount;
    private int repsCount;

}