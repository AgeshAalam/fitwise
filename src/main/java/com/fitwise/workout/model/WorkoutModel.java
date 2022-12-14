package com.fitwise.workout.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fitwise.model.circuit.CircuitScheduleModel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkoutModel implements Serializable{
	
	/**
	 * Default Serialization Id
	 */
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("workoutId")
	private Long workoutId;

	@JsonProperty("imageId")
	private Long imageId;
	
	@JsonProperty("title")
	private String title;
	
	@JsonProperty("description")
	private String description;
		
	@JsonProperty("reflectAcrossPrograms")
	private Boolean reflectChangesAcrossPrograms;

	@JsonProperty("circuitSchedules")
	private List<CircuitScheduleModel> circuitSchedules;
	
}
