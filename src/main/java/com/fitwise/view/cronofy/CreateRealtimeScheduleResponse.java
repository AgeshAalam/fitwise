package com.fitwise.view.cronofy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRealtimeScheduleResponse {
	
	
	@JsonProperty("oauth")
    private RealtimeScheduleoauth realtimeScheduleoauth;
	
	@JsonProperty("event")
    private RealtimeScheduleevent realtimeScheduleevent;
	
	@JsonProperty("availability")
    private RealtimeScheduleavailability realtimeScheduleavailability;
	
	@JsonProperty("event_creation")
    private String eventcreation;
	
	@JsonProperty("target_calendars")
    private List<RealtimeScheduletargetcalendars> targetcalendars;
	
	@JsonProperty("callback_url")
    private String callbackUrl;
	
	@JsonProperty("redirect_urls")
    private RealtimeScheduleredirecturls realtimeScheduleredirecturls;
}

