package com.fitwise.view.member;

import lombok.Data;

/*
 * Created by Vignesh G on 12/07/20
 */
@Data
public class ExerciseCompletionResponse {

    private Boolean isWorkoutCompleted;
    private Boolean isWorkoutCompletedNow;
    private Boolean isProgramRatingAllowed;
    private Float programRating;

}
