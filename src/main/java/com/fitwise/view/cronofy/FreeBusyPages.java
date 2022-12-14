package com.fitwise.view.cronofy;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FreeBusyPages {
	
	@JsonProperty("current")
    private String current;
	
	@JsonProperty("total")
    private String total;
	
	@JsonProperty("next_page")
    private String next_page;
}
