package com.fitwise.response.packaging;

import lombok.Data;

import java.util.List;

@Data
public class MemberSessionView {


    private long userKloudlessMeetingId;

    private long packageSessionMappingId;

    private String meetingTitle;

    private long meetingTypeId;

    private String meetingType;

    private String meetingId;

    private boolean isCompleted;

    private boolean isMaximumBookingsReachedForWeek;

    private boolean isBookingRestricted;

    private int noOfAvailableSessions;

    private List<ScheduleView> schedules;
}
