package com.fitwise.view.cronofy;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RealtimeScheduleResponse {
	
	@JsonProperty("url")
    private String url;
	
	@JsonProperty("real_time_scheduling")
    private Realtimescheduling realtimescheduling;
}