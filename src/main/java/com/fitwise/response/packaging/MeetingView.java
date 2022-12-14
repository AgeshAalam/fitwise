package com.fitwise.response.packaging;

import com.fitwise.entity.calendar.CalendarMeetingType;
import com.fitwise.response.LocationResponse;
import lombok.Data;

/*
 * Created by Vignesh G on 22/09/20
 */
@Data
public class MeetingView {

    private Long meetingId;

    private Long packageSessionMappingId;

    private Long order;

    private String title;

    private CalendarMeetingType calendarMeetingType;

    private Integer countPerWeek;

    private Integer durationMinutes;

    private Integer totalSessions;

    private LocationResponse location;

    private String meetingUrl;
    
    private String isSchedule;

}
