package com.fitwise.view.cronofy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeSchedulerequiredduration {
	@JsonProperty("minutes")
    private String minutes;
}
