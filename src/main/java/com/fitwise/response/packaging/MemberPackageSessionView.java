package com.fitwise.response.packaging;

import lombok.Data;

@Data
public class MemberPackageSessionView {

    private long userKloudlessScheduleId;

    private long userKloudlessMeetingId;

    private long packageSessionMappingId;

    private String meetingTitle;

    private long meetingTypeId;

    private Long selectedMeetingTypeIdInSchedule;

    private String meetingType;

    private String meetingId;

    private Object schedulePayload;

    private boolean isCompleted;

    private boolean isMaximumBookingsReachedForWeek;

    private boolean isBookingRestricted;

    private int noOfAvailableSessions;
    
    private Boolean isRescheduled;
}
