package com.fitwise.view.calendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvailabilityTimeWindow {

    @JsonProperty("start")
    public String start;

    @JsonProperty("end")
    public String end;

}
