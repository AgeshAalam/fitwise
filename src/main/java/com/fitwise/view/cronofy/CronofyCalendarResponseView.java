package com.fitwise.view.cronofy;

import java.util.List;

import lombok.Data;

@Data
public class CronofyCalendarResponseView {
	
	private String accountId;

	private boolean isActive;

	private String accountEmail;

	private String accountToken;
	
	private String profileId;

	private List<CronofyCalendarModel> cronofyCalendars;

}	
