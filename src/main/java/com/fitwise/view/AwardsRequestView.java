package com.fitwise.view;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AwardsRequestView {

    private Long userId;

    private Long awardsId;
    private String issuedDate;
    private String awardsTitle;
    private String organizationRecognized;
    private String externalSiteLink;
    private Long awardImageId;


}
