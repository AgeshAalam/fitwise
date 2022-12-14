package com.fitwise.view.cronofy;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeScheduletargetcalendars {
	 @JsonProperty("sub")
     private String sub;
	
	 @JsonProperty("calendar_id")
     private String calendarId;
}

