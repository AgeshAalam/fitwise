package com.fitwise.view.program;

import lombok.Data;

import java.util.List;

/*
 * Created by Vignesh G on 18/05/20
 */
@Data
public class RestActivityResponse {

    private Long activityId;

    private String activityName;

    private String iconUrl;

    private List<RestActivityMetricView> metrics;

}
