package com.fitwise.model.circuit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitwise.workout.model.ExerciseMappingModel;
import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 11/05/20
 */
@Data
public class CircuitModel {

    private Long circuitId;

    private String title;

    private boolean isAudio;

    //private long imageId;

    @JsonProperty("exerciseSchedules")
    private List<ExerciseMappingModel> exerciseSchedules;

    private Boolean reflectChangesAcrossWorkouts;

    @JsonProperty("voiceOverIds")
    private List<Long> voiceOverIds;

}
