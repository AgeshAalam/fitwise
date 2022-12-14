package com.fitwise.program.model;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fitwise.entity.discounts.OfferCodeDetail;

import java.util.List;


import lombok.Data;

@Data
public class ProgramModel implements Serializable{
	/**
	 * Default Serialization Id
	 */
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("programId")
	private Long programId;
	
	@JsonProperty("userId")
	private Long userId;
	
	@JsonProperty("title")
	private String title;

	@JsonProperty("shortDescription")
	private String shortDescription;

	@JsonProperty("description")
	private String description;
	
	@JsonProperty("programTypeId")
	private Long programTypeId;
	
	@JsonProperty("expertiseId")
	private Long expertiseId;
	
	@JsonProperty("durationId")
	private Long durationId;
	
	@JsonProperty("programTypeLevelGoalMappingIds")
	private List<Long> programTypeLevelGoalMappingIds;
	
	@JsonProperty("imageId")
	private Long imageId;

	@JsonProperty("promotionId")
	private Long promotionId;

	@JsonProperty("workoutIds")
	private List<Long> workoutIds;
	
	@JsonProperty("workoutSchedules")
	private List<ScheduleModel> workoutSchedules;
	
	@JsonProperty("programPrice")
	private Double programPrice;

	private List<ProgramPlatformPriceModel> programPlatformPriceModels;

	private boolean isSaveAsDraft;
	
	@JsonProperty("discountOffersIds")
	private List<Long> discountOffersIds;
	
	@JsonProperty("programSubTypeId")
	private Long programSubTypeId;

}
