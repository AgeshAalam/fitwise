package com.fitwise.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class WorkoutExercises {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long workoutExerciseId;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "workout_id")
	private Workouts workout;

	@ManyToOne(cascade = CascadeType.DETACH)
	@JoinColumn(name = "exercise_id")
	private Exercises exercise;

	private Long loopCount;

	private Long restTime;
}
