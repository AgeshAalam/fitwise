package com.fitwise.view;


import com.fitwise.entity.Images;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


/**
 * Class used to populate list of member detail in Admin web app
 */

@Getter
@Setter
public class MemberProgramTileView {

    private Long programId;

    private String programTitle;

    private Images thumbnail;

    private String subscriptionStatus;

    private Long duration;

    private String progress;

    private long progressPercent;

    private Date subscribedDate;
    private String subscribedDateFormatted;

    private Date initialStartDate;
    private String initialStartDateFormatted;

    private Date startDate;
    private String startDateFormatted;

    private Date completedDate;
    private String completedDateFormatted;

}
