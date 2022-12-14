package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Event {
	
	@JsonProperty("summary")
    private String summary;
	
	@JsonProperty("event_id")
    private String eventId;
	
	@JsonProperty("event_private")
    private String eventPrivate;
}