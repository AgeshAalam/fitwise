package com.fitwise.entity.calendar;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
public class ZoomMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long zoomMeetingId;

    private String meetingId;

    private String meetingType;

    private String occurrenceId;

    private String topic;

    private Date startTime;

    private String timezone;

    private Long durationInMinutes;

    private String joinUrl;

    @ManyToOne
    @JoinColumn(name = "zoom_account_id")
    private ZoomAccount zoomAccount;

}
