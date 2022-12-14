package com.fitwise.service.calendar;

import lombok.Data;

@Data
public class SchedulePayload {

    private String name;

    private String description;

    private String start;

    private String start_time_zone;

    private String end;
}
