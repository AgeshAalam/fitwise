package com.fitwise.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class Workouts.
 */
@Entity
@Getter
@Setter
public class Workouts {

	/** The workout id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long workoutId;
	
	/** The title. */
	private String title;
	
	/** The description. */
	private String description;

	/** The image. */
	@ManyToOne(cascade = {CascadeType.MERGE,CascadeType.DETACH})
	@JoinColumn(name = "image_id")
	private Images image;
	
	/** The flag. */
	private boolean flag;
	
	/** The owner. */
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "user_id")
	private User owner;
	
	/** The exercise scheduler. */
	@OneToMany(mappedBy = "workout", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<ExerciseSchedulers> exerciseScheduler= new HashSet<>();

	@OneToMany(mappedBy = "workout", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Set<CircuitSchedule> circuitSchedules= new HashSet<>();

}
