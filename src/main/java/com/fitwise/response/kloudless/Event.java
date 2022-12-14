package com.fitwise.response.kloudless;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Event {

    @JsonProperty("api")
    @SerializedName("api")
    private String api;

    @JsonProperty("type")
    @SerializedName("type")
    private String type;

    @JsonProperty("id")
    @SerializedName("id")
    private String id;

    @JsonProperty("account_id")
    @SerializedName("account_id")
    private String accountId;

    @JsonProperty("calendar_id")
    @SerializedName("calendar_id")
    private String calendarId;

    @JsonProperty("ical_uid")
    @SerializedName("ical_uid")
    private String icalUid;

    @JsonProperty("recurrence_type")
    @SerializedName("recurrence_type")
    private String recurrenceType;

    @JsonProperty("recurring_master_id")
    @SerializedName("recurring_master_id")
    private String recurringMasterId;

    @JsonProperty("recurrence")
    @SerializedName("recurrence")
    private List<Recurrence> recurrence = null;

    @JsonProperty("creator")
    @SerializedName("creator")
    private Organizer creator;

    @JsonProperty("organizer")
    @SerializedName("organizer")
    private Organizer organizer;

    @JsonProperty("on_organizer_calendar")
    @SerializedName("on_organizer_calendar")
    private Boolean onOrganizerCalendar;

    @JsonProperty("attendees")
    @SerializedName("attendees")
    private List<Attendee> attendees = null;

    @JsonProperty("created")
    @SerializedName("created")
    private String created;

    @JsonProperty("modified")
    @SerializedName("modified")
    private String modified;

    @JsonProperty("all_day")
    @SerializedName("all_day")
    private Boolean allDay;

    @JsonProperty("start")
    @SerializedName("start")
    private String start;

    @JsonProperty("start_time_zone")
    @SerializedName("start_time_zone")
    private String startTimeZone;

    @JsonProperty("end")
    @SerializedName("end")
    private String end;

    @JsonProperty("end_time_zone")
    @SerializedName("end_time_zone")
    private String endTimeZone;

    @JsonProperty("original_start")
    @SerializedName("original_start")
    private String originalStart;

    @JsonProperty("original_start_time_zone")
    @SerializedName("original_start_time_zone")
    private String originalStartTimeZone;

    @JsonProperty("name")
    @SerializedName("name")
    private String name;

    @JsonProperty("description")
    @SerializedName("description")
    private String description;

    @JsonProperty("location")
    @SerializedName("location")
    private String location;

    @JsonProperty("status")
    @SerializedName("status")
    private String status;

    @JsonProperty("custom_properties")
    @SerializedName("custom_properties")
    private List<CustomProperty> customProperties = null;

    @JsonProperty("reminders")
    @SerializedName("reminders")
    private List<Reminder> reminders = null;

    @JsonProperty("use_default_reminder")
    @SerializedName("use_default_reminder")
    private Boolean useDefaultReminder;

    @JsonProperty("href")
    @SerializedName("href")
    private String href;

}
