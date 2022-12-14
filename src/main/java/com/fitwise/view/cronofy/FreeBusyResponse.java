package com.fitwise.view.cronofy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FreeBusyResponse {
	
	@JsonProperty("pages")
    private FreeBusyPages freebusypages;
	
    @JsonProperty("free_busy")
    private List<FreeBusyEvent> freebusyevents;
}
