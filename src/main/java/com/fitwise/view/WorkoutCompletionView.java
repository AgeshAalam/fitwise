package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class WorkoutCompletionView {

    private  boolean workoutCompleted;
    private String completedDate;
    private Date completedDateTimeStamp;
}
