package com.fitwise.response;

import java.util.List;

import com.fitwise.exercise.model.UploadModel;
import com.fitwise.model.ExerciseCategoryModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VimeoVideoView {

	private Long imageId;
	private String thumbnailUrl;
	private long videoId;
	private String uri;
	private String name;
	private int duration;
	private UploadModel upload;
	private String videoUploadStatus;
	private List<ExerciseCategoryModel> videoExerciseCategories;

}
