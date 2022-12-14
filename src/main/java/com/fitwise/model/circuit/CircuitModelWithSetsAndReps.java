package com.fitwise.model.circuit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitwise.workout.model.ExerciseMappingModelWithSetsAndReps;
import lombok.Data;

import java.util.List;

@Data
public class CircuitModelWithSetsAndReps {

    private Long circuitId;

    private String title;

    private boolean isAudio;

    //private long imageId;

    @JsonProperty("exerciseMappingModelWithSetsAndReps")
    private List<ExerciseMappingModelWithSetsAndReps> exerciseMappingModelWithSetsAndReps;

    private Boolean reflectChangesAcrossWorkouts;

    @JsonProperty("voiceOverIds")
    private List<Long> voiceOverIds;
}
