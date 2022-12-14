package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCalendarResponse {
	
	
	@JsonProperty("calendar")
    private CalendarModelResponse calendar;
}
