package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeschedulingStatusResponse {
	
	@JsonProperty("real_time_scheduling_id")
    private String realtimeschedulingId;
	
	@JsonProperty("url")
    private String url;
	
	@JsonProperty("event")
    private EventschedulingStatusResponse eventStatusResponse;
	
	@JsonProperty("status")
    private String status;

}