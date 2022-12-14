package com.fitwise.program.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ScheduleModel implements Serializable{
	
	/**
	 *  Default Id
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("scheduleId")
	private Long scheduleId;
	
	@JsonProperty("order")
	private Long order;
	
	@JsonProperty("restDay")
	private boolean restDay;
	
	@JsonProperty("workoutId")
	private Long workoutId;
	
	@JsonProperty("title")
	private String title;

	private RestActivityScheduleModel restActivityModel;
}