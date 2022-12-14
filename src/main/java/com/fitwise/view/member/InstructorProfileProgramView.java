package com.fitwise.view.member;

import lombok.Data;

import java.util.Date;

/*
 * Created by Vignesh G on 19/05/20
 */
@Data
public class InstructorProfileProgramView {

    private Long programId;

    private String title;

    private Date createdDate;

    private String createdDateFormatted;

    private Long imageId;

    private String thumbnailUrl;

    private String programPrice;

    private String formattedProgramPrice;

    private String expertiseLevel;

    private Long duration;

    private int numberOfCurrentAvailableOffers;

    private boolean freeToAccess = false;

}
