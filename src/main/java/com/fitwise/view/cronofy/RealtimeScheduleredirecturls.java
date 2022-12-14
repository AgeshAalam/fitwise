package com.fitwise.view.cronofy;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RealtimeScheduleredirecturls {
	
	@JsonProperty("completed_url")
     private String completedUrl;
}
