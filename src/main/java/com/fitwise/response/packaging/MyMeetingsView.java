package com.fitwise.response.packaging;

import lombok.Data;

@Data
public class MyMeetingsView {

    private Long fitwiseMeetingId;

    private String meetingId;

    private Long meetingTypeId;

    private String meetingType;

    private String name;

    private Integer sessionDuration;

    private Integer durationInMinutes;

    private boolean isUsedInPackage;

}
