package com.fitwise.view.calendar;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ZoomMeetingRequest {

    private String title;

    private String startTime;

    private Integer duration;

    private String timeZone;

    private List<String> recurrenceRules;

}
