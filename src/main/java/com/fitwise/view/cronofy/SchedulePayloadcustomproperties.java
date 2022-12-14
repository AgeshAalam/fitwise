package com.fitwise.view.cronofy;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SchedulePayloadcustomproperties {

	 @JsonProperty("key")
     private String key;
	 
	 @JsonProperty("value")
     private String value;
	 
	 @JsonProperty("private")
     private Boolean Private;
}
