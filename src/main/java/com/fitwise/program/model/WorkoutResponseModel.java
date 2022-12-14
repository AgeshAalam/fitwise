package com.fitwise.program.model;

import lombok.Data;

@Data
public class WorkoutResponseModel {


	private Long workoutId;
	private Long imageId;
	private String thumbnailUrl;
	private String title;
	private String description;
	private int exerciseCount;
	private int circuitCount;
	private int duration;
	private boolean isVideoProcessingPending;
	private boolean containsBlockedExercise;

}
