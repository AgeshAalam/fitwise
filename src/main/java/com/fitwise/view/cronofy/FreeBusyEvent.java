package com.fitwise.view.cronofy;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FreeBusyEvent {

	@JsonProperty("calendar_id")
    private String calendar_id;
	
	@JsonProperty("start")
    private String start;
	
	@JsonProperty("end")
    private String end;
	
	@JsonProperty("free_busy_status")
    private String free_busy_status;
}
