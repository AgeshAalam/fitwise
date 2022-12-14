package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchedulePayloadattendees {
	
	 @JsonProperty("id")
     private String id;
	
	 @JsonProperty("name")
     private String name;
	 
	 @JsonProperty("email")
     private String email;
	 
	 @JsonProperty("status")
     private String status;
	 
	 @JsonProperty("required")
     private String required;
	
	 @JsonProperty("resource")
     private String resource;
	 
}
