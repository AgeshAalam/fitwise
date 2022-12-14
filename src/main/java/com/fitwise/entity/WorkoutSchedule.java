package com.fitwise.entity;

import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;

/**
 * The Class WorkoutSchedule.
 */
@Entity
@Getter
@Setter
public class WorkoutSchedule {

	/** The workout schedule id. */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "workout_schedule_id")
	private Long workoutScheduleId;

	/** The title. */
	@Column(name = "title")
	private String title;

	/** The order. */
	@Column(name = "workout_order")
	private Long order;
	
	/** The workout. */
	@ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
	@JoinColumn(name = "workoutId")
	private Workouts workout;
	
	/** The is rest day. */
	@Column(name= "isRestDay")
	private boolean isRestDay;
	
	/** The programs. */
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
	@JoinColumn(name = "programId")
	private Programs programs;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private InstructorRestActivity instructorRestActivity;
	
}
