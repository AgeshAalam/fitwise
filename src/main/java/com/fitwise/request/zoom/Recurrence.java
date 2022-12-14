package com.fitwise.request.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Recurrence {

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("repeat_interval")
    private Integer repeatInterval;

    @JsonProperty("weekly_days")
    private String weeklyDays;

    @JsonProperty("monthly_day")
    private Integer monthlyDay;

    @JsonProperty("monthly_week")
    private Integer monthlyWeek;

    @JsonProperty("monthly_week_day")
    private Integer monthlyWeekDay;

    @JsonProperty("end_times")
    private Integer endTimes;

    @JsonProperty("end_date_time")
    private String endDateTime;

}
