package com.fitwise.model;

import lombok.Data;

import java.util.Date;

@Data
public class InstructorUnavailabilityRequestModel {

    private Long instructorUnavailabilityId;

    private Long startDate;

    private Long endDate;
}
