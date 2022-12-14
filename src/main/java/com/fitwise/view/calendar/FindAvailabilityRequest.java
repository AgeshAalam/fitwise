package com.fitwise.view.calendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FindAvailabilityRequest {

    @JsonProperty("fitwiseMeetingId")
    public Long fitwiseMeetingId;

    @JsonProperty("timeWindows")
    public List<AvailabilityTimeWindow> availabilityTimeWindows = null;

}
