package com.fitwise.admin.service;

import com.fitwise.constants.Constants;
import com.fitwise.constants.ValidationMessageConstants;
import com.fitwise.entity.Workouts;
import com.fitwise.exception.ApplicationException;
import com.fitwise.repository.WorkoutRepository;
import com.fitwise.workout.model.WorkoutResponseView;
import com.fitwise.workout.service.WorkoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Process all the workout process for admin
 */
@Service
public class AdminWorkoutService {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private WorkoutService workoutService;

    /**
     * Get the workout for the given workoutId
     * @param workoutId
     * @return WorkoutResponseView
     */
    public WorkoutResponseView getWorkout(final Long workoutId) {
        Workouts workout = workoutRepository.findByWorkoutId(workoutId);
        if(workout == null){
            throw new ApplicationException(Constants.BAD_REQUEST, ValidationMessageConstants.MSG_WORKOUT_NOT_FOUND, null);
        }
        return workoutService.constructWorkoutResponseView(workout);
    }
}
