package com.fitwise.program.model;

import lombok.Data;

@Data
public class WorkoutScheduleModel {

	private Long scheduleId;
	private Long workoutId;
	private Long imageId;
	private String thumbnailUrl;
	private String title;
	private String description;
	private boolean isRestDay;
	private Long order;
	private int circuitCount;
	private long duration;
	private String day;
	private RestActivityScheduleModel restActivityModel;
	private boolean isVideoProcessingPending;
	private boolean containsBlockedExercise;

}