package com.fitwise.view.cronofy;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CronofyWeeklyPeriods {
	   
	   @JsonProperty("day")
	   private String day;
	   
	   @JsonProperty("start_time")
	   private String starttime;
	  
	   @JsonProperty("end_time")
	   private String endtime;
}
