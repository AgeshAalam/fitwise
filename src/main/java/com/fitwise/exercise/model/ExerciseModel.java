package com.fitwise.exercise.model;

import com.fitwise.entity.Equipments;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class ExerciseModel implements Serializable{

	/**
	 * Default Serialization Id
	 */
	private static final long serialVersionUID = 1L;
	
	private Long exerciseId;

	/**  The file name */
	private String fileName;

	/** The file size */
	private Long fileSize;

	/**  The title */
	private String title;
	
	/**  The description */
	private String description;
	
	/** The Instructor Id. */
	private Long imageId;
	
	private String thumbnailUrl;
	
	/**
	 * Vimeo Data
	 */
	private VimeoModel vimeoData;

	private List<Equipments> equipments;

	private long duration;

	private String videoUploadStatus;

	private SupportingVideoModel supportingVideoModel;

	private Boolean isDeleteSupportVideo;

	private Boolean isExerciseBlocked;
	
    private Long videoId;

}
