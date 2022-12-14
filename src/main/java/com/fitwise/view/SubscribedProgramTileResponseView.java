package com.fitwise.view;

import com.fitwise.entity.PlatformType;
import com.fitwise.entity.subscription.SubscriptionType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class SubscribedProgramTileResponseView {
    private String programName;
    private Long programId;
    private String programLevel;
    private Long programDuration;
    private String subscribedDate;
    private Date subscribedDateTimeStamp;
    private boolean isSubscriptionOn = false;
    private String programThumbnail;
    private PlatformType platformType;
    private String subscriptionType;
    private long programCount;
    private long sessionCount;
}
