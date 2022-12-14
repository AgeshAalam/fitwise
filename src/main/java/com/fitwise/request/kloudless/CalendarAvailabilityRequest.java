package com.fitwise.request.kloudless;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CalendarAvailabilityRequest {

    @JsonProperty("calendars")
    private List<String> calendars = null;

    @JsonProperty("meeting_duration")
    private String meetingDuration;

    @JsonProperty("time_windows")
    private List<TimeWindow> timeWindows = null;

}
