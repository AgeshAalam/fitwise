package com.fitwise.specifications.jpa.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class WorkoutCompletionDAO {

    private Long programId;

    private Long workoutCompletionCount;

    private Date firstWorkoutCompletionDate;

    private Date lastWorkoutCompletionDate;
}
