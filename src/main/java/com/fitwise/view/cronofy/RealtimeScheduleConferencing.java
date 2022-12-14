package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeScheduleConferencing {
	
	@JsonProperty("profile_id")
    private String profile_id;

	@JsonProperty("provider_description")
    private String provider_description;

	@JsonProperty("join_url")
    private String join_url;

}
