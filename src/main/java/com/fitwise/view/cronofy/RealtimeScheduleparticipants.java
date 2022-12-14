package com.fitwise.view.cronofy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeScheduleparticipants {
	@JsonProperty("members")
    private List<RealtimeSchedulemembers> members;
	
	@JsonProperty("required")
    private String required;
	
	
	
}
