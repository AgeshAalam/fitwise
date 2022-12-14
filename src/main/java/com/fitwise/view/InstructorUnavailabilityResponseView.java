package com.fitwise.view;


import lombok.Data;

import java.util.Date;

@Data
public class InstructorUnavailabilityResponseView {

    private Long instructorUnavailabilityId;

    private String startDate;

    private String endDate;

    private Object schedulePayload;

}
