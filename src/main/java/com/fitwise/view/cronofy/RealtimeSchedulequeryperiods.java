package com.fitwise.view.cronofy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeSchedulequeryperiods {
	@JsonProperty("start")
    private String start;
	
	@JsonProperty("end")
    private String end;
}
