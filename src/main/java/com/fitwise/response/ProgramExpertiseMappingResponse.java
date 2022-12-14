package com.fitwise.response;

import com.fitwise.entity.ExpertiseLevels;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ProgramExpertiseMappingResponse {

	private long programTypeId;
	private String programTypeName;
	private String iconUrl;
	
	private List<ExpertiseLevels> expertiseLevel;


}
