package com.fitwise.view.cronofy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeSchedulemembers {
	
	@JsonProperty("sub")
    private String sub;
	
	@JsonProperty("calendar_ids")
    private List<String> calendarids;
}