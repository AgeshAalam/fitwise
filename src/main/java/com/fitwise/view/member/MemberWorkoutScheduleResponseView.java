package com.fitwise.view.member;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Response view for rendering Workout under Program detail page in Client side
 */
@Getter
@Setter
public class MemberWorkoutScheduleResponseView {

    private Long workoutScheduleId;
    private Long workoutId;
    private String workoutThumbnail;
    private String displayTitle;
    private String workoutTitle;
    private int totalCircuits;
    private int duration;
    private boolean isTrial;
    private boolean isWorkoutCompleted;
    private String workoutCompletedOn;
    private Date workoutCompletionDate;
    private String workoutCompletionDateFormatted;
    private Date previousWorkoutCompletionDate;
    private boolean isRestDay;
    private boolean isRestActivity;
    private Long order;
    private boolean isTodayWorkout;
    private boolean isVideoProcessingPending;

}
