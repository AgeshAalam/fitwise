package com.fitwise.response.kloudless;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingWindow {

    @SerializedName("id")
    private String id;

    @SerializedName("booking_calendar_id")
    private String bookingCalendarId;

    @SerializedName("duration")
    private Integer duration;

    @SerializedName("title")
    private String title;

    @SerializedName("organizer")
    private String organizer;

    @SerializedName("location")
    private String location;

    @SerializedName("description")
    private String description;

    @SerializedName("availability")
    private Availability availability;

    @SerializedName("time_buffer_before")
    private Integer timeBufferBefore;

    @SerializedName("time_buffer_after")
    private Integer timeBufferAfter;

    @SerializedName("time_slot_interval")
    private Integer timeSlotInterval;

    @SerializedName("availability_range")
    private Integer availabilityRange;

    @SerializedName("time_zone")
    private String timeZone;

    @SerializedName("api")
    private String api;

    @SerializedName("allow_event_metadata")
    private Boolean allowEventMetadata;

    @SerializedName("default_event_metadata")
    private JsonObject defaultEventMetadata;

}
