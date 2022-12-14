package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Eventschedulingstarttime {
	
	@JsonProperty("time")
    private String time;
	
	@JsonProperty("tzid")
    private String tzid;
	
}
