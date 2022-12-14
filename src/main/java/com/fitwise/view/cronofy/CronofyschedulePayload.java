package com.fitwise.view.cronofy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CronofyschedulePayload {
    
	@JsonProperty("api")
    private String api;
	
	@JsonProperty("type")
    private String type;
	
	@JsonProperty("id")
    private String id;
	
	@JsonProperty("account_id")
    private String accountId;
	
	@JsonProperty("calendar_id")
    private String calendarId;
	
	@JsonProperty("ical_uid")
    private String icalUid;
	
	@JsonProperty("recurrence_type")
    private String recurrenceType;
	
	@JsonProperty("creator")
    private SchedulePayloadcreator creator;
	
    @JsonProperty("organizer")
    private SchedulePayloadorganizer organizer;
	
    @JsonProperty("on_organizer_calendar")
    private Boolean onorganizerCalendar;
	
    @JsonProperty("attendees")
    private List<SchedulePayloadattendees> attendees;
	
    @JsonProperty("created")
    private String created;
	
    @JsonProperty("modified")
    private String modified;
	
    @JsonProperty("all_day")
    private Boolean allDay;
	
    @JsonProperty("start")
    private String start;
	
    @JsonProperty("start_time_zone")
    private String startimezone;
	
    @JsonProperty("end")
    private String end;
	
    @JsonProperty("end_time_zone")
    private String endtimezone;
	
    @JsonProperty("name")
    private String name;
	
    @JsonProperty("location")
    private String location;
	
    @JsonProperty("attachments")
    private List<SchedulePayloadattachments> attachments;
	
    @JsonProperty("custom_properties")
    private List<SchedulePayloadcustomproperties> custom_properties;
	
    @JsonProperty("use_default_reminder")
    private Boolean usedefaultreminder;
	
    @JsonProperty("reminders")
    private List<SchedulePayloadreminders> reminders;
	
    @JsonProperty("href")
    private String href;
}