package com.fitwise.view.cronofy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RequestavailabilityrulesResponse {
	
    @JsonProperty("availability_rule_id")
    private String availabilityRuleId;

    @JsonProperty("tzid")
    private String tzid;

    @JsonProperty("calendar_ids")
    private List<String> calendarIds;

    @JsonProperty("weekly_periods")
    private List<Object> weeklyPeriods;
}

