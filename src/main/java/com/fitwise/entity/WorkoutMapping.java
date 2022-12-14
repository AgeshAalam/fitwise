package com.fitwise.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class WorkoutMapping.
 */
@Entity
@Getter
@Setter
public class WorkoutMapping {

	/** The workout mapping id. */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long workoutMappingId;

	/** The trial. */
	private boolean trial;
	
	/** The workout. */
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
	@JoinColumn(name = "workoutId")
	private Workouts workout;

	/** The programs. */
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
	@JoinColumn(name = "programId")
	private Programs programs;
}	
