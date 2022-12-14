package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventschedulingStatusResponse {
	
	@JsonProperty("summary")
    private String summary;
	
	@JsonProperty("event_id")
    private String eventId;
	
	@JsonProperty("start")
    private Eventschedulingstarttime eventschedulingstarttime;

	@JsonProperty("end")
    private Eventschedulingendtime eventschedulingendtime;
	
	@JsonProperty("event_private")
    private String eventPrivate;
}
