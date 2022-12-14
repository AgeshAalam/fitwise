package com.fitwise.repository;

import com.fitwise.entity.Exercises;
import com.fitwise.entity.WorkoutExercises;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutExercisesRepository extends JpaRepository<WorkoutExercises,Long>{

    List<WorkoutExercises> findByWorkoutWorkoutId(final Long workoutId);

    WorkoutExercises findByWorkoutWorkoutIdAndExerciseExerciseId(final Long workoutId,final Long exerciseId);

    void deleteByExercise(Exercises exercises);
}
