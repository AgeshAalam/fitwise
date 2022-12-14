package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetScheduleStatusResponse {
	
	 @JsonProperty("url")
     private String url;
	
	 @JsonProperty("real_time_scheduling")
     private RealtimeschedulingStatusResponse realtimeschedulingStatusResponse;
}
