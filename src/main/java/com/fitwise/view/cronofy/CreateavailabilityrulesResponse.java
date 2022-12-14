package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateavailabilityrulesResponse {
	   
	    @JsonProperty("availability_rule")
	    private AvailabilityruleModelResponse availabilityrule;    
}
