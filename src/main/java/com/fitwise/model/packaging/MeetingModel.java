package com.fitwise.model.packaging;

import lombok.Data;

/*
 * Created by Vignesh G on 22/09/20
 * Updated by Naveen L on 20/09/21
 */
@Data
public class MeetingModel {

    private Long meetingId;

    private Long order;

    private int countPerWeek;

    private int countPerPackage;

    private String title;

    private Long locationId;

    private String meetingUrl;
}
