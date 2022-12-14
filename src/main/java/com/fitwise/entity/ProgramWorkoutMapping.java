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
public class ProgramWorkoutMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long ProgramWorkoutId;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "program_id")
	private Programs program;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "workout_id")
	private Workouts workout;
	
	private String workoutDisplayName;
}
