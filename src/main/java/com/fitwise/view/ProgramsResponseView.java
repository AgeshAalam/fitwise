package com.fitwise.view;

import com.fitwise.entity.Duration;
import com.fitwise.entity.Images;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ProgramsResponseView {

    private long programId;

    private String programTitle;

    private String thumbnailUrl;

    private String status;

    private String subscriptionStatus;

    private int completedDays;

    private int duration;

    private long progressPercent;

    private Date subscribedDate;
    private String subscribedDateFormatted;

    private Date startedDate;
    private String startedDateFormatted;

    private Date completedDate;
    private String completedDateFormatted;


}
