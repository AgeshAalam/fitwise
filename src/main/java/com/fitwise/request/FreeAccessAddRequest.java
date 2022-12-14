package com.fitwise.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FreeAccessAddRequest {

	private List<Long> memberIdList;
	private List<Long> programIdList;
	private List<Long> packageIdList;
	private boolean infiniteTimeDuration;
	private String freeAccessStartDate;
	private String freeAccessEndDate;

}
