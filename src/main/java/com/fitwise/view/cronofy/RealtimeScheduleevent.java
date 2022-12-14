package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeScheduleevent {
	
	@JsonProperty("event_id")
    private String eventId;
	
	@JsonProperty("summary")
    private String summary;
	
	@JsonProperty("tzid")
    private String tzid;
	
	@JsonProperty("conferencing")
    private RealtimeScheduleConferencing realtimeScheduleConferencing;

	@JsonProperty("description")
    private String description;
	
	@JsonProperty("location")
    private RealtimeScheduleLocation realtimeScheduleLocation;

}
