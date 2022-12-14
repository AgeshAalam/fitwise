package com.fitwise.response.packaging;

import com.fitwise.entity.packaging.SessionType;
import com.fitwise.response.MemberLocationResponse;
import lombok.Data;

@Data
public class SessionMemberView {

    private Long fitwiseMeetingId;

    private Long packageSessionMappingId;

    private Long order;

    private String title;

    private long meetingTypeId;

    private String meetingType;

    private Integer totalNoOfSessions;

    private Integer noOfBookedSessions;

    private Integer noOfAvailableSessions;

    private Integer noOfCompletedSessions;

    private Integer countPerWeek;
    
    private Integer durationMinutes;

    private MemberLocationResponse location;

}
