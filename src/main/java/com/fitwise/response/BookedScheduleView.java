package com.fitwise.response;

import lombok.Data;

@Data
public class BookedScheduleView {

    private Long kloudlessScheduleId;

    private long kloudlessMetingId;

    private String sessionTitle;

    private long meetingTypeId;

    private String meetingType;

    private String sessionTitleInPackage;

    private String memberName;

    private Object schedulePayload;
}
