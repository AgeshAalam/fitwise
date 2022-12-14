package com.fitwise.view.instructor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstructorExerciseResponseView {
    private long exerciseId;
    private String exerciseThumbnail;
    private Long order;
    private String exerciseVideoUrl;
    private String exerciseTitle;
    private int exerciseDuration;
    private boolean isRest;
    private Long restDuration;
    private Long loopCount;
}
