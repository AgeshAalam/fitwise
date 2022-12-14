package com.fitwise.view.calendar;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KloudlessMeetingModel {

    private Long fitwiseMeetingId;

    private String calendarId;

    private String meetingId;

    private Long meetingTypeId;

    private String meetingType;

    private String name;

    private Integer sessionDuration;

    private Integer durationInMinutes;

    private String timeZone;

    private Object meetingWindow;

    private boolean isUsedInPackage;
    
    private Long cronofyavailabilityrulesid;
	   
    private String bufferbefore;
   
    private String bufferafter;
    
    private String startinterval;
    
    private List<Object> weeklyperiods;

}
