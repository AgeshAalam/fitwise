package com.fitwise.workout.daoImpl;

import java.util.List;

import com.fitwise.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitwise.entity.Workouts;
import com.fitwise.repository.WorkoutRepository;

@Service
public class WorkoutRepositoryImpl {

	@Autowired
	private WorkoutRepository workoutRepo;

	public Workouts saveWorkout(Workouts exercise) {

		return workoutRepo.save(exercise);
	}

	public List<Workouts> findByWorkoutIdAndOwner(final Long workoutId, final User user){
		return workoutRepo.findByWorkoutIdAndOwner(workoutId, user);
	}

	public List<Workouts> findByOwnerUserId(final User user){
		return workoutRepo.findByOwner(user);
	}

	/**
	 * Find Workout by owner and title
	 *
	 * @param userId
	 * @param title
	 * @return Workout by owner and title
	 */
	public Workouts findByOwnerUserIdAndTitle(long userId, String title) {
		return workoutRepo.findByOwnerUserIdAndTitleIgnoreCase(userId, title);
	}

}
