package com.fitwise.view.cronofy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CronofyCalendarModel {

	    private String providerName;

	    private String profileId;

	    private String profileName;

	    private String calendarId;
	    
	    private String calendarName;
	    
	    private Boolean defaultCalendar;
	   
	    private Boolean calendarReadonly;
	   
	    private Boolean calendarDeleted;
	    
	    private Boolean calendarIntegratedConferencingAvailable;
	  
	    private Boolean calendarPrimary;
	    
	    private String permissionLevel;
}
