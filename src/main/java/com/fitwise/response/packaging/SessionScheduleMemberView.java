package com.fitwise.response.packaging;

import lombok.Data;

import java.util.Date;

@Data
public class SessionScheduleMemberView {

    private Long fitwiseScheduleId;
    private String meetingId;
    private Long subscriptionPackageId;
    private Date bookingDate;
    private String schedulePayload;
}
