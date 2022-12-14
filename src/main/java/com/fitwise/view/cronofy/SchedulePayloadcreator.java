package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchedulePayloadcreator {
	
	@JsonProperty("id")
    private String id;
	
	@JsonProperty("name")
    private String name;
	
	@JsonProperty("email")
    private String email;

}