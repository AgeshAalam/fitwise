package com.fitwise.view.cronofy;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CronofyMeetingModel {
	   
	    private Long fitwiseMeetingId;

	    private String calendarId;

	    private String meetingId;

	    private Long meetingTypeId;

	    private String meetingType;

	    private String name;
	    
	    private String eventDescription;

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
	    
	    private List<Object> availability;
	   
}
