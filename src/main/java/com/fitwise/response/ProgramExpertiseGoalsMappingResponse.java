package com.fitwise.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProgramExpertiseGoalsMappingResponse {
	private long programTypeId;
	//private long programSubTypeId;
	private String programType;
	//private String programSubTypeName;
	private Long expertiseLevelId;
	private String expertiseLevel;
	private List<ProgramGoalsView> programGoals;
}
