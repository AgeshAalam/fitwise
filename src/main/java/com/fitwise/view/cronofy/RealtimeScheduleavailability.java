package com.fitwise.view.cronofy;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeScheduleavailability {
	
	@JsonProperty("participants")
    private List<RealtimeScheduleparticipants> participants;
	
	@JsonProperty("required_duration")
    private RealtimeSchedulerequiredduration requiredduration;
	
	@JsonProperty("query_periods")
    private List<RealtimeSchedulequeryperiods> queryperiods;
	
	@JsonProperty("start_interval")
    private RealtimeSchedulestartinterval startinterval;
	
	@JsonProperty("buffer")
    private RealtimeSchedulebuffer buffer;
	
	@JsonProperty("max_results")
    private String maxresults;
	
}
