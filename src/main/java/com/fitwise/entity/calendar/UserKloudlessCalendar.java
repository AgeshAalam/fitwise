package com.fitwise.entity.calendar;

import com.fitwise.entity.User;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class UserKloudlessCalendar {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long userKloudlessCalendarId;

     @ManyToOne
     private User user;

     private String calendarName;

     @ManyToOne
     private UserKloudlessAccount userKloudlessAccount;

     private String calendarId;

     private Boolean primaryCalendar;
    
     private String providerName;
    
     private String  profileId;

	 private String  profileName;
	 
	 private Boolean calendarPrimary;
  
	 private Boolean calendarReadonly;
  
	 private Boolean calendarDeleted;
	
	 private Boolean calendarIntegratedConferencingAvailable;
	
	 private String  permissionLevel;
}
