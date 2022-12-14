package com.fitwise.view.circuit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitwise.view.AudioResponseView;
import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 13/05/20
 */
@Data
public class CircuitScheduleResponseView {

    private Long CircuitScheduleId;

    private Long circuitId;

    private String circuitTitle;

    //private String circuitThumbnailUrl;

    private Long order;

    private Long repeat;

    private Long restBetweenRepeat;

    private long duration;

    private int exerciseCount;

    private List<String> exerciseThumbnails;

    private boolean isRestCircuit;

    private boolean isVideoProcessingPending;

    private boolean isAudio;

    @JsonProperty("audioResponseView")
    private List<AudioResponseView> audioResponseView;
}
