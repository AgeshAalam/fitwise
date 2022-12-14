package com.fitwise.workout.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitwise.entity.Equipments;
import com.fitwise.view.RestResponseView;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

@Data
public class ExerciseMappingModel implements Serializable, Comparable<ExerciseMappingModel> {

	/**
	 *  Default Serialization Id
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("scheduleId")
	private Long scheduleId;

	@JsonProperty("exerciseId")
	private long exerciseId;
	
	@JsonProperty("title")
	private String title;
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("isRestVideo")
	private boolean restVideo;
	
	@JsonProperty("url")
	private String url;

	@JsonProperty("parsedUrl")
	private String parsedUrl;

	@JsonProperty("order")
	private Long order;

	@JsonProperty("loopCount")
	private long loopCount;
	
	/** The Thumbnail Url. */
	@JsonProperty("thumbnailUrl")
	private String thumbnailUrl;

	/** The Image Id. */
	@JsonProperty("imageId")
	private Long imageId;

	@JsonProperty("duration")
	private int duration;


	private RestResponseView workoutRestVideo;

	@JsonProperty("equipments")
	private List<Equipments> equipments;

	@JsonProperty("videoUploadStatus")
	private String videoUploadStatus;

	private SupportingVideoMappingModel supportingVideoMappingModel;

	@JsonProperty("isAudio")
	private boolean audio;

	@JsonProperty("voiceOverId")
	private Long voiceOverId;

	@Override
	public int compareTo(@NotNull ExerciseMappingModel exerciseMappingModel) {
		return getOrder().compareTo(exerciseMappingModel.getOrder());
	}
}
