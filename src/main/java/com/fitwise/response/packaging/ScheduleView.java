package com.fitwise.response.packaging;

import lombok.Data;

@Data
public class ScheduleView {

    private long userKloudlessScheduleId;

    private Object schedulePayload;

    private Long selectedMeetingTypeIdInSchedule;

    private String meetingType;

    private Boolean isRescheduled;

}
