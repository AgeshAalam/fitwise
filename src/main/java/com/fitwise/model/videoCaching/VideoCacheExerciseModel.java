package com.fitwise.model.videoCaching;


import lombok.Data;


@Data
public class VideoCacheExerciseModel {

    private Long exerciseScheduleId;

    private Long exerciseCompletionDateInMillis;

    private Long flagReasonId;

    private Long circuitAndVoiceOverMappingId;

    private boolean isAudio;

    private Long circuitScheduleId;
}
