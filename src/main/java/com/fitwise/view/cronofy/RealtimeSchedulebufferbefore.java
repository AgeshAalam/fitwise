package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeSchedulebufferbefore {
	@JsonProperty("minutes")
    private String minutes;

}
