package com.fitwise.entity;

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
public class MemberProgramData {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long memberProgramDataId;
	
	/**
	 * pId - ProgramId
	 */
	@ManyToOne
	@JoinColumn(name = "program_id")
	private Programs program;
	
	/*
	 * wId - WorkoutId
	 */
	@ManyToOne
	@JoinColumn(name = "workout_id")
	private Workouts workout;
	
	/**
	 * exId - ExerciseId
	 */
	@ManyToOne
	@JoinColumn(name = "exercise_id")
	private Exercises exercise;
	
	private int completionStatus;
	
	private boolean isFlagged;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
}
