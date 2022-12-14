package com.fitwise.view.circuit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitwise.view.AudioResponseView;
import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 12/05/20
 */
@Data
public class CircuitResponseView {

    private Long circuitId;
    private String title;
    private Long instructorId;
    private long duration;
    private int exerciseCount;
    private String status;
    //private String thumbnailUrl;

    List<ExerciseScheduleView> exerciseSchedules;

    private boolean isAudio;

    @JsonProperty("audioResponseViews")
    private List<AudioResponseView> audioResponseViews;

}
