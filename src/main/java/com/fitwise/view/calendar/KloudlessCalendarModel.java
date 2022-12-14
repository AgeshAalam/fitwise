package com.fitwise.view.calendar;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KloudlessCalendarModel {

    private String calendarId;

    private Boolean defaultCalendar;

    private String calendarName;

    private String accountId;
  
    private String providerName;
   
    private String profileId;

    private String profileName;

    private Boolean calendarReadonly;

    private Boolean calendarDeleted;

    private Boolean calendarIntegratedConferencingAvailable;

    private Boolean calendarPrimary;

    private String permissionLevel;
}
