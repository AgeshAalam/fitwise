package com.fitwise.request.zoom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Meeting {

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("id")
    private Long id;

    @JsonProperty("host_id")
    private String hostId;

    @JsonProperty("host_email")
    private String hostEmail;

    @JsonProperty("topic")
    private String topic;

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("status")
    private String status;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("schedule_for")
    private String scheduleFor;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("start_url")
    private String startUrl;

    @JsonProperty("join_url")
    private String joinUrl;

    @JsonProperty("agenda")
    private String agenda;

    @JsonProperty("password")
    private String password;

    @JsonProperty("h323_password")
    private String h323Password;

    @JsonProperty("pstn_password")
    private String pstnPassword;

    @JsonProperty("encrypted_password")
    private String encryptedPassword;

    @JsonProperty("occurrences")
    private List<Occurrence> occurrences;

    @JsonProperty("recurrence")
    private Recurrence recurrence;

    @JsonProperty("settings")
    private Settings settings;

}
