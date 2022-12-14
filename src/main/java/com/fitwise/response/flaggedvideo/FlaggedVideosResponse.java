package com.fitwise.response.flaggedvideo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class FlaggedVideosResponse {

    private long exerciseId;
    private String exerciseTitle;
    private long flaggedCount;
    private Date firstFlaggedtime;
    private String firstFlaggedtimeFormatted;
    private Date latestFlaggedtime;
    private String latestFlaggedtimeFormatted;
    private String flagStatus;
    private String exerciseVideoId;
    private String exerciseThumbnailUrl;
}
