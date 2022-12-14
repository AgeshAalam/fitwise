package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RealtimeScheduleoauth {

	@JsonProperty("redirect_uri")
    private String redirectUri;
	
}