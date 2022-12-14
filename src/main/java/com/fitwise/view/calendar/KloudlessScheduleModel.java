package com.fitwise.view.calendar;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

@Getter
@Setter
public class KloudlessScheduleModel {

    private Long fitwiseScheduleId;
    private Long packageSessionMappingId;
    private Long fitwiseMeetingId;
    private Long fitwiseMeetingTypeId;
    private String scheduleId;
    private String redirectUri;
    private String meetingId;
    private Long subscriptionPackageId;
    private Object schedulePayload;
    private Date bookingDate;
    private Boolean isRescheduled;
}
