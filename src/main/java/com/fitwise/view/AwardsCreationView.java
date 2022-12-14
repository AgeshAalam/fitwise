package com.fitwise.view;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AwardsCreationView {


    private Long userId;
    private String issuedDate;
    private String awardsTitle;
    private String organizationRecognized;
    private String externalSiteLink;
    private Long awardImageId;

}
