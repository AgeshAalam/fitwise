package com.fitwise.view.cronofy;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstructorUnavailabilityschedulepayload {
	
	@JsonProperty("api")
    private String api;
	
	@JsonProperty("type")
    private String type;
	
	@JsonProperty("id")
    private String id;
	
	@JsonProperty("account_id")
    private String account_id;
	
	@JsonProperty("calendar_id")
    private String calendar_id;
	
	@JsonProperty("ical_uid")
    private String ical_uid;
	
	@JsonProperty("recurrence_type")
    private String recurrence_type;
	
	@JsonProperty("creator")
    private SchedulePayloadcreator creator;
	
    @JsonProperty("organizer")
    private SchedulePayloadorganizer organizer;
	
    @JsonProperty("on_organizer_calendar")
    private Boolean on_organizer_calendar;
	
    @JsonProperty("attendees")
    private List<SchedulePayloadattendees> attendees;
	
    @JsonProperty("created")
    private String created;
	
    @JsonProperty("modified")
    private String modified;
	
    @JsonProperty("all_day")
    private Boolean all_day;
	
    @JsonProperty("start")
    private String start;
	
    @JsonProperty("start_time_zone")
    private String start_time_zone;
	
    @JsonProperty("end")
    private String end;
	
    @JsonProperty("end_time_zone")
    private String end_time_zone;
    
	
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("status")
    private String status;
	
    @JsonProperty("location")
    private String location;
	
    @JsonProperty("attachments")
    private List<SchedulePayloadattachments> attachments;
	
    @JsonProperty("custom_properties")
    private List<SchedulePayloadcustomproperties> custom_properties;
	
    @JsonProperty("use_default_reminder")
    private Boolean use_default_reminder;
	
    @JsonProperty("reminders")
    private List<SchedulePayloadreminders> reminders;
	
    @JsonProperty("href")
    private String href;
}