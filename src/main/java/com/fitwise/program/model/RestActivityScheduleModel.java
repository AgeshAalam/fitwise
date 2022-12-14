package com.fitwise.program.model;

import lombok.Data;

/*
 * Created by Vignesh G on 18/05/20
 */
@Data
public class RestActivityScheduleModel {

    private Long activityId;

    private String activityName;

    private Long metricId;

    private Long value;
    
    private String notes;
}
